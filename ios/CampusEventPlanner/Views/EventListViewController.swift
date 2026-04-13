import UIKit

/// Displays the list of campus events in a UITableView.
///
/// Data is fetched from the Flask backend via `APIClient.fetchEvents()`.
class EventListViewController: UITableViewController {

    private var events: [Event] = []

    // MARK: - Lifecycle

    override func viewDidLoad() {
        super.viewDidLoad()

        title = "Campus Events"
        tableView.register(UITableViewCell.self, forCellReuseIdentifier: "EventCell")

        // TODO (students): add a UISearchController for search/filter

        loadEvents()
    }

    // MARK: - Data loading

    private func loadEvents() {
        APIClient.fetchEvents { [weak self] result in
            switch result {
            case .success(let events):
                self?.events = events
                self?.tableView.reloadData()
            case .failure(let error):
                print("Failed to load events: \(error.localizedDescription)")
                // TODO (students): show an alert or retry button
            }
        }
    }

    // MARK: - UITableViewDataSource

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return events.count
    }

    override func tableView(_ tableView: UITableView,
                            cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "EventCell", for: indexPath)
        let event = events[indexPath.row]

        var config = cell.defaultContentConfiguration()
        config.text = event.title
        config.secondaryText = "\(event.date) — \(event.location ?? "")"
        cell.contentConfiguration = config
        cell.accessoryType = .disclosureIndicator

        return cell
    }

    // MARK: - UITableViewDelegate

    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)

        let event = events[indexPath.row]
        let detailVC = EventDetailViewController()
        detailVC.eventId = event.id
        navigationController?.pushViewController(detailVC, animated: true)
    }
}
