/* Show the number of lessons given per month during a specified year. 
   It shall be possible to retrieve the total number of lessons per month 
   (just one number per month) and the specific number of individual lessons, 
   group lessons and ensembles (three numbers per month). This query is 
   expected to be performed a few times per week. */
   
WITH given_lessons_in_selected_year as (
	SELECT *
	FROM music_lesson
	WHERE extract (year FROM appointed_time) = 2021 AND executed = true
),
lessons_per_month as (
	SELECT COUNT(extract (month FROM appointed_time)), extract (month FROM appointed_time) as month, type_id
	FROM given_lessons_in_selected_year
	GROUP BY month, type_id
),
lessons_pivoted as (
	SELECT month, 
		   round(avg(CASE WHEN type_id = 1 THEN count END), 0) AS individual,
		   round(avg(CASE WHEN type_id = 2 THEN count END), 0) AS group,
	       round(avg(CASE WHEN type_id = 3 THEN count END), 0) AS ensamble
	FROM lessons_per_month
	GROUP BY month
	ORDER BY month
),
total_per_month as (
	SELECT SUM(count) as total, month
	FROM lessons_per_month
	GROUP BY month
)

SELECT lessons_pivoted.month, lessons_pivoted.individual, lessons_pivoted.group, lessons_pivoted.ensamble, total
FROM lessons_pivoted JOIN total_per_month ON lessons_pivoted.month = total_per_month.month
ORDER BY month