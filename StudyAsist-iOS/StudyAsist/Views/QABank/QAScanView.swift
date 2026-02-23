//
//  QAScanView.swift
//  StudyAsist
//
//  Camera/gallery + Vision OCR to extract text for Q&A
//

import SwiftUI
import Vision
import PhotosUI

struct QAScanView: View {
    let onTextExtracted: (String) -> Void
    @Environment(\.dismiss) private var dismiss
    @State private var selectedItem: PhotosPickerItem?
    @State private var extractedText = ""
    @State private var isProcessing = false
    @State private var showingCamera = false
    @State private var capturedImage: UIImage?
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 20) {
                if let img = capturedImage {
                    Image(uiImage: img)
                        .resizable()
                        .scaledToFit()
                        .frame(maxHeight: 300)
                        .cornerRadius(8)
                }
                
                if isProcessing {
                    ProgressView("Extracting text...")
                }
                
                if !extractedText.isEmpty {
                    TextEditor(text: $extractedText)
                        .frame(minHeight: 150)
                        .padding(8)
                        .overlay(
                            RoundedRectangle(cornerRadius: 8)
                                .stroke(Color.gray.opacity(0.3), lineWidth: 1)
                        )
                }
                
                HStack(spacing: 16) {
                    Button {
                        showingCamera = true
                    } label: {
                        Label("Camera", systemImage: "camera")
                    }
                    
                    PhotosPicker(selection: $selectedItem, matching: .images) {
                        Label("Gallery", systemImage: "photo")
                    }
                    .onChange(of: selectedItem) { _, new in
                        if let new {
                            loadAndProcessImage(from: new)
                        }
                    }
                }
            }
            .padding()
            .navigationTitle("Scan Q&A")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Use Text") {
                        onTextExtracted(extractedText)
                        dismiss()
                    }
                    .disabled(extractedText.isEmpty)
                }
            }
            .fullScreenCover(isPresented: $showingCamera) {
                CameraPickerView { image in
                    capturedImage = image
                    showingCamera = false
                    processImage(image)
                } onCancel: {
                    showingCamera = false
                }
            }
        }
    }
    
    private func loadAndProcessImage(from item: PhotosPickerItem) {
        isProcessing = true
        item.loadTransferable(type: Data.self) { result in
            switch result {
            case .success(let data):
                if let data, let img = UIImage(data: data) {
                    Task { @MainActor in
                        capturedImage = img
                        processImage(img)
                    }
                }
            case .failure:
                break
            }
            Task { @MainActor in
                isProcessing = false
            }
        }
    }
    
    private func processImage(_ image: UIImage) {
        isProcessing = true
        guard let cgImage = image.cgImage else {
            isProcessing = false
            return
        }
        
        let request = VNRecognizeTextRequest { request, _ in
            let observations = request.results as? [VNRecognizedTextObservation] ?? []
            let text = observations.compactMap { $0.topCandidates(1).first?.string }.joined(separator: "\n")
            Task { @MainActor in
                extractedText = text
                isProcessing = false
            }
        }
        request.recognitionLevel = .accurate
        
        Task.detached(priority: .userInitiated) {
            let handler = VNImageRequestHandler(cgImage: cgImage, options: [:])
            try? handler.perform([request])
        }
    }
}

struct CameraPickerView: UIViewControllerRepresentable {
    let onCapture: (UIImage) -> Void
    let onCancel: () -> Void
    
    func makeUIViewController(context: Context) -> UIImagePickerController {
        let picker = UIImagePickerController()
        picker.sourceType = .camera
        picker.delegate = context.coordinator
        return picker
    }
    
    func updateUIViewController(_ uiViewController: UIImagePickerController, context: Context) {}
    
    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }
    
    class Coordinator: NSObject, UIImagePickerControllerDelegate, UINavigationControllerDelegate {
        let parent: CameraPickerView
        
        init(_ parent: CameraPickerView) {
            self.parent = parent
        }
        
        func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey: Any]) {
            if let img = info[.originalImage] as? UIImage {
                parent.onCapture(img)
            }
            picker.dismiss(animated: true)
        }
        
        func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
            parent.onCancel()
            picker.dismiss(animated: true)
        }
    }
}
