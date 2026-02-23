//
//  AuthService.swift
//  StudyAsist
//

import Foundation
import AuthenticationServices

@MainActor
final class AuthService: NSObject, ObservableObject {
    static let shared = AuthService()
    
    @Published var isSignedIn: Bool = false
    @Published var errorMessage: String?
    
    private var continuation: CheckedContinuation<String, Error>?
    
    private override init() {
        super.init()
        isSignedIn = KeychainService.shared.getJWT() != nil
    }
    
    func signInWithApple() async throws {
        let provider = ASAuthorizationAppleIDProvider()
        let request = provider.createRequest()
        request.requestedScopes = [.fullName, .email]
        
        let controller = ASAuthorizationController(authorizationRequests: [request])
        controller.delegate = self
        controller.performRequests()
        
        let token = try await withCheckedThrowingContinuation { (cont: CheckedContinuation<String, Error>) in
            continuation = cont
        }
        
        let client = ApiClient(baseURL: SettingsStore.shared.backendBaseURL)
        let response: TokenResponse = try await client.request(
            path: "api/auth/apple",
            method: "POST",
            body: IdTokenRequest(id_token: token),
            authenticated: false
        )
        KeychainService.shared.saveJWT(response.accessToken)
        isSignedIn = true
    }
    
    func signOut() {
        KeychainService.shared.deleteJWT()
        isSignedIn = false
    }
}

extension AuthService: ASAuthorizationControllerDelegate {
    nonisolated func authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithAuthorization authorization: ASAuthorization
    ) {
        guard let credential = authorization.credential as? ASAuthorizationAppleIDCredential,
              let tokenData = credential.identityToken,
              let token = String(data: tokenData, encoding: .utf8) else {
            Task { @MainActor in
                continuation?.resume(throwing: AuthError.noIdentityToken)
            }
            return
        }
        Task { @MainActor in
            continuation?.resume(returning: token)
            continuation = nil
        }
    }
    
    nonisolated func authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithError error: Error
    ) {
        Task { @MainActor in
            continuation?.resume(throwing: error)
            continuation = nil
        }
    }
}

struct IdTokenRequest: Encodable {
    let id_token: String
}

struct TokenResponse: Decodable {
    let accessToken: String
    
    enum CodingKeys: String, CodingKey {
        case accessToken = "access_token"
    }
}

enum AuthError: Error {
    case noIdentityToken
}
