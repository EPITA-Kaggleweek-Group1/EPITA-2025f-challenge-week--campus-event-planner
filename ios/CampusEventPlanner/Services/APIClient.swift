import Foundation

/// Lightweight networking layer for the Campus Event Planner API.
///
/// Uses URLSession (no third-party dependencies). Base URL points to
/// localhost:5000 by default.
class APIClient {

    /// Change this to your Mac's LAN IP when testing on a physical device.
    static let baseURL = "http://localhost:5000"

    // MARK: - Events

    /// Fetch all events from `GET /events`.
    ///
    /// - Parameter completion: Called on the main queue with the result.
    static func fetchEvents(completion: @escaping (Result<[Event], Error>) -> Void) {
        guard let url = URL(string: "\(baseURL)/events") else {
            completion(.failure(APIError.invalidURL))
            return
        }

        URLSession.shared.dataTask(with: url) { data, response, error in
            if let error = error {
                DispatchQueue.main.async { completion(.failure(error)) }
                return
            }

            guard let data = data else {
                DispatchQueue.main.async { completion(.failure(APIError.noData)) }
                return
            }

            do {
                let events = try JSONDecoder().decode([Event].self, from: data)
                DispatchQueue.main.async { completion(.success(events)) }
            } catch {
                DispatchQueue.main.async { completion(.failure(error)) }
            }
        }.resume()
    }

    /// Fetch a single event by ID from `GET /events/<id>`.
    ///
    /// - Parameters:
    ///   - id: The event's primary key.
    ///   - completion: Called on the main queue with the result.
    static func fetchEvent(id: Int, completion: @escaping (Result<Event, Error>) -> Void) {
        guard let url = URL(string: "\(baseURL)/events/\(id)") else {
            completion(.failure(APIError.invalidURL))
            return
        }

        URLSession.shared.dataTask(with: url) { data, response, error in
            if let error = error {
                DispatchQueue.main.async { completion(.failure(error)) }
                return
            }

            guard let data = data else {
                DispatchQueue.main.async { completion(.failure(APIError.noData)) }
                return
            }

            do {
                let event = try JSONDecoder().decode(Event.self, from: data)
                DispatchQueue.main.async { completion(.success(event)) }
            } catch {
                DispatchQueue.main.async { completion(.failure(error)) }
            }
        }.resume()
    }

    // MARK: - Errors

    enum APIError: LocalizedError {
        case invalidURL
        case noData

        var errorDescription: String? {
            switch self {
            case .invalidURL: return "Invalid URL"
            case .noData:     return "No data received from the server"
            }
        }
    }
}
