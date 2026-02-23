//
//  ApiClient.swift
//  StudyAsist
//

import Foundation

struct ApiClient {
    let baseURL: String
    
    init(baseURL: String? = nil) {
        self.baseURL = (baseURL ?? ProcessInfo.processInfo.environment["STUDYASIST_BACKEND_URL"] ?? "http://localhost:8000").trimmingCharacters(in: CharacterSet(charactersIn: "/")) + "/"
    }
    
    func request<T: Decodable>(
        path: String,
        method: String = "GET",
        authenticated: Bool = true
    ) async throws -> T {
        try await _request(path: path, method: method, body: nil as Data?, authenticated: authenticated)
    }
    
    func request<T: Decodable, B: Encodable>(
        path: String,
        method: String = "GET",
        body: B,
        authenticated: Bool = true
    ) async throws -> T {
        let data = try JSONEncoder().encode(body)
        return try await _request(path: path, method: method, body: data, authenticated: authenticated)
    }
    
    private func _request<T: Decodable>(
        path: String,
        method: String,
        body: Data?,
        authenticated: Bool
    ) async throws -> T {
        guard let url = URL(string: baseURL + path) else {
            throw ApiError.invalidURL
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = method
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        
        if authenticated, let token = KeychainService.shared.getJWT() {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        
        if let body = body {
            request.httpBody = body
        }
        
        let (data, response) = try await URLSession.shared.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse else {
            throw ApiError.invalidResponse
        }
        
        guard (200...299).contains(httpResponse.statusCode) else {
            throw ApiError.httpStatus(httpResponse.statusCode, String(data: data, encoding: .utf8))
        }
        
        return try JSONDecoder().decode(T.self, from: data)
    }
    
}

private struct EmptyResponse: Decodable {}

enum ApiError: Error {
    case invalidURL
    case invalidResponse
    case httpStatus(Int, String?)
}
