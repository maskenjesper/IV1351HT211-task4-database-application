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
         SELECT COUNT(extract (month FROM appointed_time)) as count, extract (month FROM appointed_time) as month, lesson_type
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

SELECT lessons_pivoted.month, COALESCE(lessons_pivoted.individual,0), COALESCE(lessons_pivoted.group,0),COALESCE( lessons_pivoted.ensamble,0), total
FROM lessons_pivoted JOIN total_per_month ON lessons_pivoted.month = total_per_month.month
ORDER BY month
