//
//  KeychainService.swift
//  StudyAsist
//

import Foundation
import Security

final class KeychainService {
    static let shared = KeychainService()
    
    private let serviceName = "com.studyasist"
    private let jwtKey = "auth_jwt_token"
    
    private init() {}
    
    func saveJWT(_ token: String) {
        guard let data = token.data(using: .utf8) else { return }
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: serviceName,
            kSecAttrAccount as String: jwtKey,
            kSecValueData as String: data
        ]
        SecItemDelete(query as CFDictionary) // Remove existing
        SecItemAdd(query as CFDictionary, nil)
    }
    
    func getJWT() -> String? {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: serviceName,
            kSecAttrAccount as String: jwtKey,
            kSecReturnData as String: true,
            kSecMatchLimit as String: kSecMatchLimitOne
        ]
        var result: AnyObject?
        let status = SecItemCopyMatching(query as CFDictionary, &result)
        guard status == errSecSuccess, let data = result as? Data else { return nil }
        return String(data: data, encoding: .utf8)
    }
    
    func deleteJWT() {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: serviceName,
            kSecAttrAccount as String: jwtKey
        ]
        SecItemDelete(query as CFDictionary)
    }
}
