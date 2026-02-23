//
//  AssessmentQuestion.swift
//  StudyAsist
//

import Foundation
import SwiftData

@Model
final class AssessmentQuestion {
    @Attribute(.unique) var id: Int64
    var assessmentId: Int64
    var qaId: Int64
    var weight: Float
    var sequence: Int
    
    init(
        id: Int64 = 0,
        assessmentId: Int64,
        qaId: Int64,
        weight: Float = 1.0,
        sequence: Int
    ) {
        self.id = id
        self.assessmentId = assessmentId
        self.qaId = qaId
        self.weight = weight
        self.sequence = sequence
    }
}
