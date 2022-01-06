CREATE type person_type AS enum ('instructor', 'student', 'parent');
CREATE type lesson_type AS enum ('ensamble', 'group_lesson', 'individual_lesson');

CREATE TABLE address
(
    address_id       SERIAL
        CONSTRAINT address_pkey
            PRIMARY KEY,
    country          VARCHAR(100) NOT NULL,
    city             VARCHAR(100) NOT NULL,
    street           VARCHAR(100) NOT NULL,
    street_number    INTEGER NOT NULL,
    zip              CHAR(10) NOT NULL,
    apartment_number VARCHAR(100)
);

CREATE TABLE person
(
    person_id SERIAL
        CONSTRAINT person_pkey
            PRIMARY KEY,
    pnr       CHAR(12) UNIQUE NOT NULL,
    name      VARCHAR(100) NOT NULL,
    age       INTEGER NOT NULL,
    person_type      person_type NOT NULL
);

CREATE TABLE address_person
(
    person_id  INTEGER
        CONSTRAINT address_person_pkey
            PRIMARY KEY
        CONSTRAINT address_person_person_id_fkey
            REFERENCES person
            ON DELETE CASCADE,
    address_id INTEGER NOT NULL
        CONSTRAINT address_person_address_id_fkey
            REFERENCES address
            ON DELETE SET NULL
);

CREATE TABLE contact_details
(
    contact_details_id SERIAL
        CONSTRAINT contact_details_pkey
            PRIMARY KEY,
    phone              CHAR(10) NOT NULL,
    email              VARCHAR(100) NOT NULL
);

CREATE TABLE instructor
(
    instructor_id       SERIAL
        CONSTRAINT instructor_pkey
            PRIMARY KEY,
    person_id           INTEGER UNIQUE NOT NULL
        CONSTRAINT instructor_person_id_fkey
            REFERENCES person
            ON DELETE CASCADE,
    contact_details_id  INTEGER UNIQUE NOT NULL
        CONSTRAINT instructor_contact_details_id_fkey
            REFERENCES contact_details
            ON DELETE SET NULL,
    can_teach_ensambles boolean NOT NULL
);

CREATE TABLE instrument_taught
(
    instrument_type VARCHAR(100),
    instructor_id   INTEGER
    CONSTRAINT instrument_taught_instructor_id_fkey
        REFERENCES instructor
        ON DELETE CASCADE,
    CONSTRAINT instrument_taught_pkey
        PRIMARY KEY (instrument_type, instructor_id)
);

CREATE TABLE timeslot
(
    timeslot_id     SERIAL
        CONSTRAINT timeslot_pkey
            PRIMARY KEY,
    start_time      TIMESTAMP NOT NULL,
    end_time        TIMESTAMP NOT NULL,
    instrument_type VARCHAR(100)
);

CREATE TABLE instructor_timeslot
(
    instructor_id INTEGER
        CONSTRAINT instructor_timeslot_instructor_id_fkey
            REFERENCES instructor
            ON DELETE CASCADE,
    timeslot_id   INTEGER
        CONSTRAINT instructor_timeslot_timeslot_id_fkey
            REFERENCES timeslot
            ON DELETE CASCADE,
    CONSTRAINT instructor_timeslot_pkey
        PRIMARY KEY (instructor_id, timeslot_id)
);

CREATE TABLE student
(
    student_id      SERIAL
        CONSTRAINT student_pkey
            PRIMARY KEY,
    person_id       INTEGER UNIQUE NOT NULL
        CONSTRAINT student_person_id_fkey
            REFERENCES person
            ON DELETE CASCADE,
    contact_details INTEGER UNIQUE NOT NULL
        CONSTRAINT student_contact_details_fkey
            REFERENCES contact_details
            ON DELETE SET NULL,
    parent_contact  INTEGER NOT NULL
        CONSTRAINT student_parent_contact_fkey
            REFERENCES contact_details
            ON DELETE SET NULL,
    parent_id       INTEGER
        CONSTRAINT student_person_person_id_fk
            REFERENCES person
            ON DELETE SET NULL
);

CREATE TABLE sibling
(
    sibling_id INTEGER
        CONSTRAINT sibling_sibling_id_fkey
            REFERENCES student
            ON DELETE CASCADE,
    student_id INTEGER
        CONSTRAINT sibling_student_id_fkey
            REFERENCES student
            ON DELETE CASCADE,
    CONSTRAINT sibling_pkey
        PRIMARY KEY (sibling_id, student_id)
);

CREATE TABLE instrument
(
    instrument_id SERIAL
        CONSTRAINT instrument_pkey
            PRIMARY KEY,
    type          VARCHAR(100) NOT NULL,
    is_rented     boolean NOT NULL,
    brand         VARCHAR(100) NOT NULL,
    sn            VARCHAR(100) UNIQUE NOT NULL,
    price         INTEGER NOT NULL
);

CREATE TABLE rental
(
    rental_id     SERIAL
        CONSTRAINT rental_pkey
            PRIMARY KEY,
    student_id    INTEGER NOT NULL
        CONSTRAINT rental_student_id_fkey
            REFERENCES student
            ON DELETE CASCADE,
    instrument_id INTEGER NOT NULL
        CONSTRAINT rental_instrument_id_fkey
            REFERENCES instrument
            ON DELETE CASCADE,
    start_time    TIMESTAMP NOT NULL,
    end_time      TIMESTAMP NOT NULL,
    with_delivery boolean,
    price         INTEGER NOT NULL
);

CREATE TABLE skill
(
    skill_id        SERIAL
        CONSTRAINT skill_pkey
            PRIMARY KEY,
    instrument_type VARCHAR(100) NOT NULL,
    level           INTEGER NOT NULL
);

CREATE TABLE student_skill
(
    student_id INTEGER
        CONSTRAINT student_skill_student_id_fkey
            REFERENCES student
            ON DELETE CASCADE,
    skill_id   INTEGER
        CONSTRAINT student_skill_skill_id_fkey
            REFERENCES skill
            ON DELETE CASCADE,
    CONSTRAINT student_skill_pkey
        PRIMARY KEY (student_id, skill_id)
);

CREATE TABLE music_lesson
(
    music_lesson_id SERIAL
        CONSTRAINT music_lesson_pkey
            PRIMARY KEY,
    instructor_id   INTEGER
        CONSTRAINT music_lesson_instructor_id_fkey
            REFERENCES instructor
            ON DELETE SET NULL,
    executed        boolean NOT NULL,
    lesson_type     lesson_type NOT NULL,
    appointed_time  TIMESTAMP NOT NULL
);

CREATE TABLE student_music_lesson
(
    music_lesson_id INTEGER
        CONSTRAINT student_music_lesson_music_lesson_id_fkey
            REFERENCES music_lesson
            ON DELETE CASCADE,
    student_id      INTEGER
        CONSTRAINT student_music_lesson_student_id_fkey
            REFERENCES student
            ON DELETE CASCADE,
    CONSTRAINT student_music_lesson_pkey
        PRIMARY KEY (music_lesson_id, student_id)
);

CREATE TABLE group_lesson
(
    music_lesson_id INTEGER
        CONSTRAINT group_lesson_pkey
            PRIMARY KEY
        CONSTRAINT group_lesson_music_lesson_id_fkey
            REFERENCES music_lesson
            ON DELETE CASCADE,
    skill_id        INTEGER NOT NULL
        CONSTRAINT group_lesson_skill_id_fkey
            REFERENCES skill
            ON DELETE SET NULL,
    min             INTEGER NOT NULL,
    max             INTEGER NOT NULL,
    instrument      VARCHAR(100) NOT NULL
);

CREATE TABLE individual_lesson
(
    music_lesson_id INTEGER
        CONSTRAINT individual_lesson_pkey
            PRIMARY KEY
        CONSTRAINT individual_lesson_music_lesson_id_fkey
            REFERENCES music_lesson
            ON DELETE CASCADE,
    skill_id        INTEGER NOT NULL
        CONSTRAINT individual_lesson_skill_id_fkey
            REFERENCES skill
            ON DELETE SET NULL,
    instrument      VARCHAR(100) NOT NULL
);

CREATE TABLE ensamble
(
    music_lesson_id INTEGER
        CONSTRAINT ensamble_pkey
            PRIMARY KEY
        CONSTRAINT ensamble_music_lesson_id_fkey
            REFERENCES music_lesson
            ON DELETE CASCADE,
    target_genre    VARCHAR(100) NOT NULL,
    min             INTEGER NOT NULL,
    max             INTEGER NOT NULL
);

CREATE TABLE lesson_price_scheme
(
    lesson_type INTEGER,
    skill_level INTEGER,
    value       INTEGER NOT NULL,
    CONSTRAINT lesson_price_scheme_pk
        PRIMARY KEY (lesson_type, skill_level)
);
