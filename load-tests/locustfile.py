from locust import HttpUser, task, between, SequentialTaskSet
import random


class UserBehavior(SequentialTaskSet):
    """Simulates a realistic user journey through the application"""

    def on_start(self):
        self.user_id = None
        self.session_token = None

    @task
    def register_user(self):
        username = f"loadtest_user_{random.randint(1000, 999999)}"
        password = "TestPassword123!"
        email = f"{username}@loadtest.com"

        response = self.client.post("/register", data={
            "prenom": "Load",
            "nom": "Test",
            "email": email,
            "telephone": "0600000000",
            "age": "25",
            "motDePasse": password,
            "confirmMotDePasse": password
        }, name="/register")

        if response.status_code == 200:
            self.username = email
            self.password = password

    @task
    def login(self):
        if hasattr(self, 'username'):
            response = self.client.post("/login", data={
                "email": self.username,
                "motDePasse": self.password
            }, name="/login")

            if response.status_code == 200:
                # Store session cookie for authenticated requests
                self.session_token = response.cookies.get('JSESSIONID')

    @task(3)
    def view_home(self):
        self.client.get("/home", name="/home")

    @task(5)
    def list_paths(self):
        self.client.get("/listpath", name="/listpath")

    @task(2)
    def view_profile(self):
        self.client.get("/profile", name="/profile")

    @task(2)
    def list_my_paths(self):
        self.client.get("/mypath", name="/mypath")

    @task(1)
    def create_path(self):
        self.client.post("/newpath", data={
            "departure": f"City_{random.randint(1, 100)}",
            "destination": f"City_{random.randint(1, 100)}",
            "date": "2025-12-01",
            "time": "10:00",
            "seats": random.randint(1, 4),
            "price": random.randint(5, 50)
        }, name="/newpath")

    @task(1)
    def logout(self):
        self.client.get("/logout", name="/logout")


class AnonymousUserBehavior(SequentialTaskSet):
    """Simulates anonymous user browsing without authentication"""

    @task(5)
    def view_home(self):
        self.client.get("/home", name="/home [anonymous]")

    @task(10)
    def list_paths(self):
        self.client.get("/listpath", name="/listpath [anonymous]")

    @task(1)
    def view_path_detail(self):
        path_id = random.randint(1, 100)
        self.client.get(f"/pathDetail?id={path_id}", name="/pathDetail [anonymous]")


class AuthenticatedUser(HttpUser):
    """Authenticated user with full access"""
    tasks = [UserBehavior]
    wait_time = between(1, 3)  # Wait 1-3 seconds between tasks
    weight = 3  # 75% of users are authenticated


class AnonymousUser(HttpUser):
    """Anonymous user just browsing"""
    tasks = [AnonymousUserBehavior]
    wait_time = between(2, 5)  # Wait 2-5 seconds between tasks
    weight = 1  # 25% of users are anonymous


class QuickLoadTest(HttpUser):
    """Quick load test for CI/CD pipeline"""
    wait_time = between(0.5, 1.5)

    @task(3)
    def home(self):
        self.client.get("/home", name="/home [quick]")

    @task(5)
    def list_paths(self):
        self.client.get("/listpath", name="/listpath [quick]")

    @task(1)
    def health_check(self):
        self.client.get("/", name="/ [health]")
