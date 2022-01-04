/* Retrieve the average number of lessons per 
   month during the entire year, instead of the total for each month. */

WITH given_lessons_in_selected_year as (
	SELECT *
	FROM music_lesson
	WHERE extract (year FROM appointed_time) = 2021 AND executed = true
),
lessons_per_month as (
	SELECT COUNT(extract (month FROM appointed_time)), extract (month FROM appointed_time) as month, lesson_type
	FROM given_lessons_in_selected_year
	GROUP BY month, lesson_type
),
lessons_pivoted as (
	SELECT month,
		   round(avg(CASE WHEN lesson_type = 'individual_lesson' THEN count END), 0) AS individual,
		   round(avg(CASE WHEN lesson_type = 'group_lesson' THEN count END), 0) AS group,
	       round(avg(CASE WHEN lesson_type = 'ensamble' THEN count END), 0) AS ensamble
	FROM lessons_per_month
	GROUP BY month
	ORDER BY month
),
total_per_month as (
	SELECT SUM(count) as total, month
	FROM lessons_per_month
	GROUP BY month
)

SELECT 	SUM(lessons_pivoted.individual) / 12 as average_individual,
		SUM(lessons_pivoted.group) / 12 as average_group,
		SUM(lessons_pivoted.ensamble) / 12 as average_ensamble,
		SUM(total) / 12 as average_total
FROM lessons_pivoted JOIN total_per_month ON lessons_pivoted.month = total_per_month.month
