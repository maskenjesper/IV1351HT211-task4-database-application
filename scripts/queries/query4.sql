/* List all ensembles held during the next week, sorted by music genre 
   and weekday. For each ensemble tell whether it's full booked, has 1-2 
   seats left or has more seats left. Hint: you might want to use a CASE 
   statement in your query to produce the desired output. */

with ensambles_next_week as (
	SELECT ensamble.music_lesson_id, target_genre, extract(dow FROM appointed_time) as dow, max
	FROM ensamble 
	JOIN music_lesson ON ensamble.music_lesson_id = music_lesson.music_lesson_id 
	WHERE extract(week FROM appointed_time) = extract(week FROM now()) AND extract(year FROM appointed_time) = extract(year FROM now())
),
student_per_lesson as (
	SELECT COUNT(student_id), ensambles_next_week.music_lesson_id, max, target_genre, dow
	FROM ensambles_next_week
	LEFT JOIN student_music_lesson ON ensambles_next_week.music_lesson_id = student_music_lesson.music_lesson_id
	GROUP BY ensambles_next_week.music_lesson_id, max, target_genre, dow
)
SELECT music_lesson_id, target_genre, dow as day_of_the_week, count as enrolled, max,
CASE 
	WHEN count < max - 2 THEN 'available'
	WHEN count < max THEN '1-2 slots left'
	ELSE 'full'
END as Status
FROM student_per_lesson 
ORDER BY dow, target_genre