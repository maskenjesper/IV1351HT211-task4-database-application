CREATE type lesson_type AS enum ('ensamble', 'group_lesson', 'individual_lesson');

CREATE TABLE music_lesson (
	music_lesson_id integer
		CONSTRAINT music_lesson_pkey
			PRIMARY KEY,
	instructor_id integer,
	price integer,
	appointed_time timestamp,
	level integer,
	instrument_type varchar(100),
	max integer,
	min integer,
	target_genre varchar(100),
	lesson_type lesson_type
);

CREATE TABLE student (
	student_id integer
		CONSTRAINT student_pkey
			PRIMARY KEY,
    music_lesson_id integer
	CONSTRAINT music_lesson_id_fkey
		REFERENCES music_lesson
			ON DELETE CASCADE
);
