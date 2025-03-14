import stomp
import websocket
import time
import json
import requests
import threading  # For handling WebSocket messages in background

BASE_URL = "http://localhost:8080/chat"
WS_ENDPOINT = "ws://localhost:8080/ws-chat"  # Or "ws://localhost:8080/ws-endpoint" if you changed it back

# --- REST API Helper Functions (from previous script) ---
def enqueue_user(user_id, country, gender, gender_preference, birth_year, interests):
    """Enqueues a user with provided details."""
    enqueue_url = f"{BASE_URL}/enqueue"
    headers = {'Content-Type': 'application/json'}
    user_data = {
        "userId": user_id,
        "country": country,
        "gender": gender,
        "genderPreference": gender_preference,
        "birthYear": birth_year,
        "interests": interests
    }
    response = requests.post(enqueue_url, headers=headers, json=user_data)
    print(f"Enqueue User {user_id}:")
    print(f"  Request URL: {enqueue_url}")
    print(f"  Status Code: {response.status_code}")
    print(f"  Response Text: {response.text}")
    assert response.status_code == 200, f"Enqueue request failed for user {user_id}"

def get_match_for_user(user_id):
    """Checks for a match for a given user."""
    get_match_url = f"{BASE_URL}/getMatch?userId={user_id}"
    response = requests.get(get_match_url)
    print(f"Get Match for User {user_id}:")
    print(f"  Request URL: {get_match_url}")
    print(f"  Status Code: {response.status_code}")
    print(f"  Response Text: {response.text}")
    if response.status_code == 200:
        print(f"  Match Response JSON: {response.json()}") # Print JSON response if status is 200
    assert response.status_code == 200, f"Get Match request failed for user {user_id}"
    return response.json() # Return JSON response for further checks

def dequeue_user(user_id):
    """Dequeues a user."""
    dequeue_url = f"{BASE_URL}/dequeue"
    headers = {'Content-Type': 'text/plain'} # Or application/json if your dequeue endpoint expects JSON body
    response = requests.post(dequeue_url, headers=headers, data=user_id) # Or json=user_id if endpoint expects JSON body
    print(f"Dequeue User {user_id}:")
    print(f"  Request URL: {dequeue_url}")
    print(f"  Status Code: {response.status_code}")
    print(f"  Response Text: {response.text}")
    assert response.status_code == 200, f"Dequeue request failed for user {user_id}"

def send_message_rest(session_id, sender_id, receiver_id, content): # Renamed to avoid conflict with WebSocket send_message
    """Sends a chat message using REST endpoint (for initial setup, not WebSocket test)."""
    send_message_url = f"{BASE_URL}/sendMessage"
    headers = {'Content-Type': 'application/json'}
    message_data = {
        "sessionId": session_id,
        "senderId": sender_id,
        "receiverId": receiver_id,
        "content": content
    }
    response = requests.post(send_message_url, headers=headers, json=message_data)
    print(f"Send Message (REST) from {sender_id} to {receiver_id}:")
    print(f"  Request URL: {send_message_url}")
    print(f"  Status Code: {response.status_code}")
    print(f"  Response Text: {response.text}")
    assert response.status_code == 200, f"Send Message (REST) request failed from {sender_id} to {receiver_id}"


# --- WebSocket Test Functions ---

class WebSocketClient:
    def __init__(self, user_id, session_id, ws_url, message_queue):
        # ... (rest of __init__ remains similar)
        self.conn = stomp.Connection12(host_and_ports=[('localhost', 8080)]) # Example host and port - adjust as needed

    def connect(self):
        self.conn.connect(login='', passcode='', wait=True) # Basic connect - adjust auth if needed
        self.conn.subscribe(destination=f'/user/{self.user_id}/queue/messages', id='sub-0', ack='auto') # Subscribe
        self.conn.set_listener('', self) # Set listener for messages
        print(f"STOMP connection and subscription initiated for user {self.user_id}")


    def on_message(self, headers, body): # Listener method for stomp.py
        try:
            message_json = json.loads(body.decode('utf-8')) # Assuming JSON messages
            self.message_queue.append(message_json) # Append JSON to queue
            print(f"Received message for user {self.user_id}: {message_json}")
        except json.JSONDecodeError:
            print(f"Received non-JSON message for user {self.user_id}: {body.decode('utf-8')}")


    def send_chat_message(self, receiver_id, content):
        message_payload = json.dumps({ # Create JSON payload
            "sessionId": self.session_id,
            "senderId": self.user_id,
            "receiverId": receiver_id,
            "content": content
        })
        self.conn.send(body=message_payload, destination='/app/chat.sendMessage', headers={'content-type': 'application/json'}) # Send message
        print(f"Sent message from {self.user_id} to {receiver_id} (WebSocket)")


    def on_error(self, headers, message): # Listener method for stomp.py error frames
        print(f'received stomp error message: {headers} / {message}')

    def on_connected(self, headers, body): # Listener method for stomp.py connected frame
        print(f'connected to stomp for user {self.user_id}, headers={headers}')

    def on_disconnected(self): # Listener method for stomp.py disconnect
        print(f'disconnected from stomp for user {self.user_id}')


    def close_connection(self):
        if self.conn and self.conn.is_connected():
            self.conn.disconnect()
        else:
            print(f"STOMP connection for user {self.user_id} already closed or not initialized.")


if __name__ == "__main__":
    user1_id = "user1"
    user2_id = "user2"

    # Enqueue users via REST API
    enqueue_user(user1_id, "USA", "FEMALE", "MALE", 1990, ["sports", "music"])
    enqueue_user(user2_id, "USA", "MALE", "FEMALE", 1992, ["music", "movies"])

    print("\n--- Waiting for matching algorithm (Simulate delay) ---")
    time.sleep(5)

    # Get matches via REST API
    match_response_user1 = get_match_for_user(user1_id)
    assert match_response_user1['matchFound'] == True, "Match check for user1 after delay should be true"
    match_response_user2 = get_match_for_user(user2_id)
    assert match_response_user2['matchFound'] == True, "Match check for user2 after delay should be true"
    session_id = match_response_user1['chatSessionId']
    matched_user_for_user1 = match_response_user1['user2Id']
    matched_user_for_user2 = match_response_user2['user2Id']
    assert matched_user_for_user1 == user2_id, f"User1 should be matched with user2"
    assert matched_user_for_user2 == user1_id, f"User2 should be matched with user1"

    # Message queues for WebSocket clients
    message_queue_user1 = []
    message_queue_user2 = []

    # Initialize and connect WebSocket clients
    client1 = WebSocketClient(user1_id, session_id, WS_ENDPOINT, message_queue_user1)
    client2 = WebSocketClient(user2_id, session_id, WS_ENDPOINT, message_queue_user2)

    print("\n--- Connecting WebSocket clients ---")
    client1.connect()
    client2.connect()

    time.sleep(2) # Wait for connections and subscriptions to be established

    print("\n--- Sending WebSocket messages ---")
    # User 1 sends message to User 2
    message_content_1_to_2 = "Hello user2 from user1 via WebSocket!"
    client1.send_chat_message(user2_id, message_content_1_to_2)

    # User 2 sends message back to User 1
    message_content_2_to_1 = "Hi user1, got your message! - User2"
    client2.send_chat_message(user1_id, message_content_2_to_1)

    time.sleep(2) # Wait for messages to be exchanged

    print("\n--- Checking received messages ---")
    received_messages_user1 = [json.loads(frame.body.decode('utf-8')) for frame in message_queue_user1]
    received_messages_user2 = [json.loads(frame.body.decode('utf-8')) for frame in message_queue_user2]

    # Assertions for received messages
    assert len(received_messages_user1) == 1, f"User1 should have received 1 message, but got {len(received_messages_user1)}"
    assert len(received_messages_user2) == 1, f"User2 should have received 1 message, but got {len(received_messages_user2)}"

    received_message_by_user1 = received_messages_user1[0]['content']
    received_message_by_user2 = received_messages_user2[0]['content']

    assert received_message_by_user1 == message_content_2_to_1, f"User1 received incorrect message: '{received_message_by_user1}', expected: '{message_content_2_to_1}'"
    assert received_message_by_user2 == message_content_1_to_2, f"User2 received incorrect message: '{received_message_by_user2}', expected: '{message_content_1_to_2}'"


    print("\n--- Closing WebSocket connections ---")
    client1.close_connection()
    client2.close_connection()

    # Dequeue users via REST API
    dequeue_user(user1_id)
    dequeue_user(user2_id)


    print("\n--- WebSocket Test Script Finished ---")
    print("All assertions passed! WebSocket message publishing test successful (if server logs show no errors).")