"""
Seed script for the Campus Event Planner database.

Inserts 15 realistic campus events with varied dates, locations, and capacities.
Run once before starting the API:

    python seed.py
"""

from config.config import get_app_db_config
from database import Database


db = Database(get_app_db_config())


EVENTS = [
    {
        "title": "Welcome Back Barbecue",
        "description": "Kick off the semester with burgers, music, and good vibes on the main quad. Free food for all students!",
        "date": "2026-03-15T12:00:00",
        "location": "Main Quad",
        "capacity": 200,
        "image_url": "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=600",
    },
    {
        "title": "Introduction to Machine Learning",
        "description": "A beginner-friendly workshop covering the basics of ML: supervised learning, decision trees, and hands-on exercises with scikit-learn.",
        "date": "2026-04-02T14:00:00",
        "location": "Amphi A — KB Building",
        "capacity": 80,
        "image_url": "https://images.unsplash.com/photo-1515879218367-8466d910adbf?w=600",
    },
    {
        "title": "Startup Pitch Night",
        "description": "Five student startups present their ideas to a panel of investors and mentors. Networking drinks afterwards.",
        "date": "2026-04-05T18:30:00",
        "location": "Innovation Hub — Room 301",
        "capacity": 120,
        "image_url": "https://images.unsplash.com/photo-1556761175-5973dc0f32e7?w=600",
    },
    {
        "title": "Campus 5K Fun Run",
        "description": "Join the annual 5K around the campus lake. All fitness levels welcome — medals for top 3 finishers!",
        "date": "2026-04-10T08:00:00",
        "location": "Sports Complex — Starting Line",
        "capacity": 150,
        "image_url": "https://images.unsplash.com/photo-1461896836934-bd45ba688509?w=600",
    },
    {
        "title": "Cybersecurity CTF Challenge",
        "description": "Capture-the-flag competition. Teams of 2-4 compete to solve security puzzles. Prizes for top 3 teams.",
        "date": "2026-04-12T10:00:00",
        "location": "Lab 204 — CS Building",
        "capacity": 60,
        "image_url": "https://images.unsplash.com/photo-1550751827-4bd374c3f58b?w=600",
    },
    {
        "title": "Film Club: Blade Runner 2049 Screening",
        "description": "Weekly film club screening followed by a group discussion. Popcorn provided.",
        "date": "2026-03-20T20:00:00",
        "location": "Lecture Hall C",
        "capacity": 100,
        "image_url": "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=600",
    },
    {
        "title": "Resume & Interview Workshop",
        "description": "Career services team walks you through resume best practices and mock interview techniques. Bring your laptop!",
        "date": "2026-04-08T15:00:00",
        "location": "Career Center — Ground Floor",
        "capacity": 40,
        "image_url": "https://images.unsplash.com/photo-1521737711867-e3b97375f902?w=600",
    },
    {
        "title": "Spring Music Festival",
        "description": "Live performances by three student bands, DJ set, and an open mic segment. Food trucks on site.",
        "date": "2026-04-18T17:00:00",
        "location": "Outdoor Amphitheater",
        "capacity": 200,
        "image_url": "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=600",
    },
    {
        "title": "Intro to Docker & Containers",
        "description": "Hands-on workshop: build, ship, and run your first containerized app. Docker Desktop required.",
        "date": "2026-04-14T14:00:00",
        "location": "Lab 102 — CS Building",
        "capacity": 35,
        "image_url": "https://images.unsplash.com/photo-1605745341112-85968b19335b?w=600",
    },
    {
        "title": "Board Game Night",
        "description": "Unwind with Catan, Codenames, Ticket to Ride, and more. Snacks and drinks provided.",
        "date": "2026-03-22T19:00:00",
        "location": "Student Lounge — Building D",
        "capacity": 30,
        "image_url": "https://images.unsplash.com/photo-1610890716171-6b1bb98ffd09?w=600",
    },
    {
        "title": "Women in Tech Panel",
        "description": "Inspiring talks from four women in the French tech industry. Q&A and networking afterwards.",
        "date": "2026-04-16T16:00:00",
        "location": "Amphi B — KB Building",
        "capacity": 100,
        "image_url": "https://images.unsplash.com/photo-1573164713988-8665fc963095?w=600",
    },
    {
        "title": "Hackathon: Green Campus",
        "description": "24-hour hackathon focused on sustainability. Build an app, hardware prototype, or data viz to make the campus greener.",
        "date": "2026-04-25T09:00:00",
        "location": "Innovation Hub — All Floors",
        "capacity": 80,
        "image_url": "https://images.unsplash.com/photo-1504384308090-c894fdcc538d?w=600",
    },
    {
        "title": "Photography Walk",
        "description": "Guided photography walk around the campus and nearby park. Tips on composition and lighting. Bring your camera or phone.",
        "date": "2026-03-10T10:00:00",
        "location": "Main Entrance Gate",
        "capacity": 20,
        "image_url": "https://images.unsplash.com/photo-1452587925148-ce544e77e70d?w=600",
    },
    {
        "title": "Yoga & Mindfulness Session",
        "description": "De-stress before exams with a guided yoga and meditation class. Mats provided, beginners welcome.",
        "date": "2026-04-20T07:30:00",
        "location": "Rooftop Terrace — Building A",
        "capacity": 25,
        "image_url": "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=600",
    },
    {
        "title": "Alumni Networking Dinner",
        "description": "Formal dinner with 20+ alumni working at top tech companies. Limited seats — register early!",
        "date": "2026-04-28T19:00:00",
        "location": "Grand Hall — Administration Building",
        "capacity": 50,
        "image_url": "https://images.unsplash.com/photo-1511795409834-ef04bbd61622?w=600",
    },
    {
        "title": "Full Capacity Event",
        "description": "This event is intentionally fully booked.",
        "date": "2026-05-01T10:00:00",
        "location": "Room X",
        "capacity": 10,
        "image_url": "",
    },
    {
        "title": "Plenty Spots Event",
        "description": "Should have more than 20 spots left.",
        "date": "2026-05-02T10:00:00",
        "location": "Room Y",
        "capacity": 50,
        "image_url": "",
    },
    {
        "title": "Medium Spots Event",
        "description": "Should have 5–10 spots left.",
        "date": "2026-05-03T10:00:00",
        "location": "Room Z",
        "capacity": 30,
        "image_url": "",
    },
    {
        "title": "Almost Full Event",
        "description": "Should have less than 5 spots left.",
        "date": "2026-05-04T10:00:00",
        "location": "Room W",
        "capacity": 20,
        "image_url": "",
    },
]


def seed_registrations():
    """Seed registrations with different fill levels."""

    conn = db.get_connection()
    cursor = conn.cursor(dictionary=True)

    total_inserted = 0  # <-- ADD

    # Helper to fetch event by title
    def get_event_id(title: str) -> int:
        cursor.execute("SELECT id FROM events WHERE title = %s", (title,))
        row = cursor.fetchone()
        if not row:
            raise RuntimeError(f"Event not found: {title}")
        return row["id"]

    def create_registrations(event_id: int, count: int):
        nonlocal total_inserted  # <-- ADD
        for i in range(count):
            cursor.execute(
                """
                INSERT INTO registrations (event_id, user_name, email)
                VALUES (%s, %s, %s)
                """,
                (
                    event_id,
                    f"User {event_id}-{i}",
                    f"user{event_id}_{i}@test.com",
                ),
            )
            total_inserted += 1  # <-- ADD

    # --- FULL
    create_registrations(get_event_id("Full Capacity Event"), 10)

    # --- MANY SPOTS LEFT
    create_registrations(get_event_id("Plenty Spots Event"), 20)

    # --- MEDIUM
    create_registrations(get_event_id("Medium Spots Event"), 22)

    # --- ALMOST FULL
    create_registrations(get_event_id("Almost Full Event"), 17)

    conn.commit()
    cursor.close()
    conn.close()

    print(f"Seeded {total_inserted} registrations.")


def seed():
    """Drop existing events and insert fresh seed data."""
    db.init_db()
    conn = db.get_connection()
    cursor = conn.cursor()

    # Clear previous seed data (but keep the schema)
    cursor.execute("DELETE FROM registrations")
    cursor.execute("DELETE FROM events")

    for event in EVENTS:
        cursor.execute(
            """
            INSERT INTO events (title, description, date, location, capacity, image_url)
            VALUES (%s, %s, %s, %s, %s, %s)
            """,
            (
                event["title"],
                event["description"],
                event["date"],
                event["location"],
                event["capacity"],
                event["image_url"],
            ),
        )

    conn.commit()
    print(f"Seeded {len(EVENTS)} events successfully.")
    conn.close()


def seed_users():
    import hashlib

    conn = db.get_connection()
    cursor = conn.cursor()
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS users (
            id INT AUTO_INCREMENT PRIMARY KEY,
            username VARCHAR(255) NOT NULL,
            password_hash VARCHAR(255) NOT NULL,
            role VARCHAR(50) DEFAULT 'user',
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    """)
    cursor.execute("DELETE FROM users")
    users = [
        ("admin", hashlib.md5(b"admin123").hexdigest(), "admin"),
        ("alice", hashlib.md5(b"password").hexdigest(), "user"),
        ("bob", hashlib.md5(b"bob2026").hexdigest(), "user"),
    ]
    for username, pw_hash, role in users:
        cursor.execute(
            "INSERT INTO users (username, password_hash, role) VALUES (%s, %s, %s)",
            (username, pw_hash, role),
        )
    conn.commit()
    cursor.close()
    conn.close()
    print(f"Seeded {len(users)} users.")


# TODO: We need to add registration seed
if __name__ == "__main__":
    seed()
    seed_users()
    seed_registrations()
