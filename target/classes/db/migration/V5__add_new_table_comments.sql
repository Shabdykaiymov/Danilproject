CREATE TABLE comments (
    id UUID PRIMARY KEY,
    route_id UUID REFERENCES routes(id),
    user_id UUID REFERENCES users(id),
    comment TEXT NOT NULL,
    create_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)