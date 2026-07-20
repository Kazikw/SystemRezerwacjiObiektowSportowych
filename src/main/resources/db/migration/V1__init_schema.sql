CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       first_name VARCHAR(100) NOT NULL UNIQUE,
                       email VARCHAR(150) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL CHECK (role IN ('USER', 'ADMIN'))
);

CREATE TABLE cities (
                        id BIGSERIAL PRIMARY KEY,
                        name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE facilities (
                            id BIGSERIAL PRIMARY KEY,
                            name VARCHAR(150) NOT NULL,
                            city_id BIGINT NOT NULL REFERENCES cities(id)
);

CREATE TABLE reservations (
                              id BIGSERIAL PRIMARY KEY,
                              facility_id BIGINT NOT NULL REFERENCES facilities(id),
                              reserver_id BIGINT NOT NULL REFERENCES users(id),
                              date DATE NOT NULL,
                              start_time TIME NOT NULL,
                              end_time TIME NOT NULL,
                              required_participants INT NOT NULL DEFAULT 1,
                              allow_join BOOLEAN NOT NULL DEFAULT FALSE,
                              status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                                  CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED')),
                              CONSTRAINT chk_reservation_time CHECK (end_time > start_time)
);

CREATE INDEX idx_reservations_facility_date ON reservations (facility_id, date);
CREATE INDEX idx_reservations_reserver ON reservations (reserver_id);

CREATE TABLE reservation_participants (
                                          reservation_id BIGINT NOT NULL REFERENCES reservations(id) ON DELETE CASCADE,
                                          user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                          PRIMARY KEY (reservation_id, user_id)
);

CREATE TABLE facility_blockades (
                                    id BIGSERIAL PRIMARY KEY,
                                    facility_id BIGINT NOT NULL REFERENCES facilities(id),
                                    start_date DATE NOT NULL,
                                    end_date DATE NOT NULL,
                                    reason TEXT NOT NULL,
                                    created_at TIMESTAMP NOT NULL DEFAULT now(),
                                    created_by_admin_id BIGINT NOT NULL REFERENCES users(id),
                                    active BOOLEAN NOT NULL DEFAULT TRUE,
                                    CONSTRAINT chk_blockade_dates CHECK (end_date >= start_date)
);

CREATE INDEX idx_blockades_facility_dates ON facility_blockades (facility_id, start_date, end_date);

CREATE TABLE friend_relations (
                                  id BIGSERIAL PRIMARY KEY,
                                  user_a_id BIGINT NOT NULL REFERENCES users(id),
                                  user_b_id BIGINT NOT NULL REFERENCES users(id),
                                  CONSTRAINT uq_friend_pair UNIQUE (user_a_id, user_b_id),
                                  CONSTRAINT chk_no_self_friend CHECK (user_a_id <> user_b_id)
);

CREATE TABLE facility_admin_assignments (
                                            id BIGSERIAL PRIMARY KEY,
                                            admin_id BIGINT NOT NULL REFERENCES users(id),
                                            facility_id BIGINT NOT NULL REFERENCES facilities(id),
                                            assigned_at TIMESTAMP NOT NULL DEFAULT now(),
                                            CONSTRAINT uq_admin_facility UNIQUE (admin_id, facility_id)
);