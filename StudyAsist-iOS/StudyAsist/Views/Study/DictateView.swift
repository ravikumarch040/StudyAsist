//
//  DictateView.swift
//  StudyAsist
//
//  OCR + TTS: Scan text from image, read aloud
//

import SwiftUI
import Vision
import AVFoundation
import PhotosUI

struct DictateView: View {
    @State private var extractedText = ""
    @State private var speechStarted = false
    @State private var selectedItem: PhotosPickerItem?
    @State private var capturedImage: UIImage?
    @State private var isProcessing = false
    
    private let synthesizer = AVSpeechSynthesizer()
    
    var body: some View {
        VStack(spacing: 16) {
            if !extractedText.isEmpty {
                TextEditor(text: $extractedText)
                    .frame(minHeight: 150)
                    .padding(8)
                
                HStack {
                    Button("Read Aloud") { speak(extractedText) }
                        .buttonStyle(.borderedProminent)
                    if speechStarted {
                        Button("Stop") {
                            synthesizer.stopSpeaking(at: .immediate)
                            speechStarted = false
                        }
                    }
                }
            }
            
            HStack(spacing: 16) {
                PhotosPicker(selection: $selectedItem, matching: .images) {
                    Label("Gallery", systemImage: "photo")
                }
                .onChange(of: selectedItem) { _, new in
                    if let new { loadImage(from: new) }
                }
            }
            
            if isProcessing {
                ProgressView("Extracting text...")
            }
        }
        .padding()
        .navigationTitle("Dictate")
        .onAppear {
            if extractedText.isEmpty {
                extractedText = "Tap Gallery to select an image, or paste text here to read aloud."
            }
        }
    }
    
    private func loadImage(from item: PhotosPickerItem) {
        isProcessing = true
        item.loadTransferable(type: Data.self) { result in
            if case .success(let data) = result, let data, let img = UIImage(data: data) {
                Task { @MainActor in
                    capturedImage = img
                    processImage(img)
                }
            }
            Task { @MainActor in isProcessing = false }
        }
    }
    
    private func processImage(_ image: UIImage) {
        guard let cgImage = image.cgImage else { return }
        let request = VNRecognizeTextRequest { req, _ in
            let text = (req.results as? [VNRecognizedTextObservation])?
                .compactMap { $0.topCandidates(1).first?.string }
                .joined(separator: "\n") ?? ""
            Task { @MainActor in extractedText = text }
        }
        request.recognitionLevel = .accurate
        try? VNImageRequestHandler(cgImage: cgImage, options: [:]).perform([request])
    }
    
    private func speak(_ text: String) {
        speechStarted = true
        let utterance = AVSpeechUtterance(string: text)
        utterance.rate = 0.5
        synthesizer.speak(utterance)
    }
}
