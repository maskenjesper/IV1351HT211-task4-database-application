create type person_type as enum ('instructor', 'student', 'parent');
create type lesson_type as enum ('ensamble', 'group_lesson', 'individual_lesson');

create table address
(
    address_id       serial
        constraint address_pkey
            primary key,
    country          varchar(100),
    city             varchar(100),
    street           varchar(100),
    street_number    integer,
    zip              char(10),
    apartment_number varchar(100)
);

create table person
(
    person_id serial
        constraint person_pkey
            primary key,
    pnr       char(12) unique,
    name      varchar(100),
    age       integer,
    person_type      person_type
);

create table address_person
(
    person_id  integer
        constraint address_person_pkey
            primary key
        constraint address_person_person_id_fkey
            references person
            on delete cascade,
    address_id integer
        constraint address_person_address_id_fkey
            references address
            on delete set null
);

create table contact_details
(
    contact_details_id serial
        constraint contact_details_pkey
            primary key,
    phone              char(10),
    email              varchar(100)
);

create table instructor
(
    instructor_id       serial
        constraint instructor_pkey
            primary key,
    person_id           integer
        constraint instructor_person_id_fkey
            references person
            on delete cascade,
    contact_details_id  integer
        constraint instructor_contact_details_id_fkey
            references contact_details
            on delete set null,
    can_teach_ensambles boolean
);

create table instrument_taught
(
    instrument_type varchar(100),
    instructor_id   integer
    constraint instrument_taught_instructor_id_fkey
        references instructor
        on delete cascade,
    constraint instrument_taught_pkey
        primary key (instrument_type, instructor_id)
);

create table timeslot
(
    timeslot_id     serial
        constraint timeslot_pkey
            primary key,
    start_time      timestamp,
    end_time        timestamp,
    instrument_type varchar(100)
);

create table instructor_timeslot
(
    instructor_id integer
        constraint instructor_timeslot_instructor_id_fkey
            references instructor
            on delete cascade,
    timeslot_id   integer
        constraint instructor_timeslot_timeslot_id_fkey
            references timeslot
            on delete cascade,
    constraint instructor_timeslot_pkey
        primary key (instructor_id, timeslot_id)
);

create table student
(
    student_id      serial
        constraint student_pkey
            primary key,
    person_id       integer
        constraint student_person_id_fkey
            references person
            on delete cascade,
    contact_details integer
        constraint student_contact_details_fkey
            references contact_details
            on delete set null,
    parent_contact  integer
        constraint student_parent_contact_fkey
            references contact_details
            on delete set null,
    parent_id       integer not null
        constraint student_person_person_id_fk
            references person
            on delete set null
);

create table sibling
(
    sibling_id integer
        constraint sibling_sibling_id_fkey
            references student
            on delete cascade,
    student_id integer
        constraint sibling_student_id_fkey
            references student
            on delete cascade,
    constraint sibling_pkey
        primary key (sibling_id, student_id)
);

create table instrument
(
    instrument_id serial
        constraint instrument_pkey
            primary key,
    type          varchar(100),
    is_rented     boolean,
    brand         varchar(100),
    sn            varchar(100),
    price         integer
);

create table rental
(
    rental_id     serial
        constraint rental_pkey
            primary key,
    student_id    integer
        constraint rental_student_id_fkey
            references student
            on delete cascade,
    instrument_id integer
        constraint rental_instrument_id_fkey
            references instrument
            on delete cascade,
    start_time    timestamp,
    end_time      timestamp,
    with_delivery boolean,
    price         integer
);

create table skill
(
    skill_id        serial
        constraint skill_pkey
            primary key,
    instrument_type varchar(100),
    level           integer
);

create table student_skill
(
    student_id integer
        constraint student_skill_student_id_fkey
            references student
            on delete cascade,
    skill_id   integer
        constraint student_skill_skill_id_fkey
            references skill
            on delete cascade,
    constraint student_skill_pkey
        primary key (student_id, skill_id)
);

create table music_lesson
(
    music_lesson_id serial
        constraint music_lesson_pkey
            primary key,
    instructor_id   integer
        constraint music_lesson_instructor_id_fkey
            references instructor
            on delete set null,
    executed        boolean,
    lesson_type     lesson_type,
    appointed_time  timestamp
);

create table student_music_lesson
(
    music_lesson_id integer
        constraint student_music_lesson_music_lesson_id_fkey
            references music_lesson
            on delete cascade,
    student_id      integer
        constraint student_music_lesson_student_id_fkey
            references student
            on delete cascade,
    constraint student_music_lesson_pkey
        primary key (music_lesson_id, student_id)
);

create table group_lesson
(
    music_lesson_id integer
        constraint group_lesson_pkey
            primary key
        constraint group_lesson_music_lesson_id_fkey
            references music_lesson
            on delete cascade,
    skill_id        integer
        constraint group_lesson_skill_id_fkey
            references skill
            on delete set null,
    min             integer,
    max             integer,
    instrument      varchar(100)
);

create table individual_lesson
(
    music_lesson_id integer
        constraint individual_lesson_pkey
            primary key
        constraint individual_lesson_music_lesson_id_fkey
            references music_lesson
            on delete cascade,
    skill_id        integer
        constraint individual_lesson_skill_id_fkey
            references skill
            on delete set null,
    instrument      varchar(100) not null
);

create table ensamble
(
    music_lesson_id integer
        constraint ensamble_pkey
            primary key
        constraint ensamble_music_lesson_id_fkey
            references music_lesson
            on delete cascade,
    target_genre    varchar(100),
    min             integer,
    max             integer
);

create table lesson_price_scheme
(
    lesson_type integer,
    skill_level integer,
    value       integer not null,
    constraint lesson_price_scheme_pk
        primary key (lesson_type, skill_level)
);
