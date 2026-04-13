import UIKit

/// Detail screen for a single event.
///
/// The layout contains placeholder labels. Data loading is NOT yet
/// implemented — students must complete this view controller.
///
/// TODO (students):
///   1. Use `APIClient.fetchEvent(id:)` to load the event
///   2. Populate the labels with real data
///   3. Format the date nicely (e.g. "Saturday 18 April 2026 at 17:00")
///   4. Show remaining spots (requires GET /events/<id>/registrations)
///   5. Add a "Register" button that calls POST /events/<id>/register
class EventDetailViewController: UIViewController {

    /// Set by the presenting view controller before navigation.
    var eventId: Int = -1

    // MARK: - UI Elements

    private let titleLabel: UILabel = {
        let label = UILabel()
        label.text = "Event Title"
        label.font = .boldSystemFont(ofSize: 24)
        label.numberOfLines = 0
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()

    private let dateLabel: UILabel = {
        let label = UILabel()
        label.text = "Date placeholder"
        label.font = .systemFont(ofSize: 16)
        label.textColor = .secondaryLabel
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()

    private let locationLabel: UILabel = {
        let label = UILabel()
        label.text = "Location placeholder"
        label.font = .systemFont(ofSize: 16)
        label.textColor = .secondaryLabel
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()

    private let capacityLabel: UILabel = {
        let label = UILabel()
        label.text = "Capacity: --"
        label.font = .systemFont(ofSize: 16)
        label.textColor = .secondaryLabel
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()

    private let descriptionLabel: UILabel = {
        let label = UILabel()
        label.text = "Description placeholder"
        label.font = .systemFont(ofSize: 15)
        label.textColor = .label
        label.numberOfLines = 0
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()

    // MARK: - Lifecycle

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .systemBackground
        title = "Event Detail"

        setupLayout()

        // TODO (students): call APIClient.fetchEvent(id: eventId) here
        // and populate the labels with the returned data.
    }

    // MARK: - Layout

    private func setupLayout() {
        let stack = UIStackView(arrangedSubviews: [
            titleLabel, dateLabel, locationLabel, capacityLabel, descriptionLabel
        ])
        stack.axis = .vertical
        stack.spacing = 12
        stack.translatesAutoresizingMaskIntoConstraints = false

        view.addSubview(stack)

        NSLayoutConstraint.activate([
            stack.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 20),
            stack.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
            stack.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20),
        ])
    }
}
