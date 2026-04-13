import Foundation

/// Represents a campus event returned by the API.
///
/// Conforms to `Codable` so it can be decoded directly from the JSON
/// returned by `GET /events`.
struct Event: Codable, Identifiable {
    let id: Int
    let title: String
    let description: String?
    let date: String
    let location: String?
    let capacity: Int
    let imageUrl: String?
    let createdAt: String?

    enum CodingKeys: String, CodingKey {
        case id, title, description, date, location, capacity
        case imageUrl = "image_url"
        case createdAt = "created_at"
    }
}
