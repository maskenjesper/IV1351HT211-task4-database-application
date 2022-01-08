/*
	Gets executed lessons from the database in denormalized form to be stored as historical data.
*/

WITH executed_lessons AS (
	SELECT *
	FROM music_lesson
	WHERE executed = true
),
executed_lessons_joined AS (
	SELECT
		executed_lessons.music_lesson_id, instructor_id,
		appointed_time, skill.level, skill.instrument_type,
		(CASE WHEN lesson_type = 'ensamble' THEN ensamble.max WHEN lesson_type = 'group_lesson' THEN group_lesson.max END) AS max,
		(CASE WHEN lesson_type = 'ensamble' THEN ensamble.min WHEN lesson_type = 'group_lesson' THEN group_lesson.min END) AS min,
		target_genre, lesson_type
	FROM executed_lessons
	LEFT JOIN group_lesson ON executed_lessons.music_lesson_id = group_lesson.music_lesson_id
	LEFT JOIN individual_lesson ON executed_lessons.music_lesson_id = individual_lesson.music_lesson_id
	LEFT JOIN ensamble ON executed_lessons.music_lesson_id = ensamble.music_lesson_id
	LEFT JOIN skill ON individual_lesson.skill_id = skill.skill_id OR group_lesson.skill_id = skill.skill_id
	ORDER BY executed_lessons.music_lesson_id
)

SELECT executed_lessons_joined.*, value AS price
FROM executed_lessons_joined
LEFT JOIN lesson_price_scheme ON
(executed_lessons_joined.lesson_type = lesson_price_scheme.lesson_type AND executed_lessons_joined.level = lesson_price_scheme.skill_level)
OR
(executed_lessons_joined.lesson_type = 'ensamble' AND executed_lessons_joined.lesson_type = lesson_price_scheme.lesson_type)