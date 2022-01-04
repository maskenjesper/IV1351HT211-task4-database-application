/* Retrieve the average number of lessons per 
   month during the entire year, instead of the total for each month. */

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
		   round(avg(CASE WHEN type_id = 'individual_lesson' THEN count END), 0) AS individual,
		   round(avg(CASE WHEN type_id = 'group_lesson' THEN count END), 0) AS group,
	       round(avg(CASE WHEN type_id = 'ensamble' THEN count END), 0) AS ensamble
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

/*************************************************************************************************/

WITH parameter as(
    select 2021 as selected_year
),
temp1 as(
    select
        music_lesson.music_lesson_id,
    lesson_type,
        music_lesson.instructor_id,
    extract(year from appointed_time) as lesson_year,
    extract(month from appointed_time) as lesson_month
    from parameter,music_lesson JOIN music_lesson_type on music_lesson.type_id=id
    WHERE  extract(year from appointed_time) = parameter.selected_year AND executed = true
), temp2 as(
    select
    parameter.selected_year,
    mnth,
    lesson_type
    FROM parameter,(SELECT generate_series(1,12,1) as mnth) t1 LEFT JOIN music_lesson_type on true
), lessons_per_month_by_type as(
    select
           count(temp1.lesson_type) as c,
            temp2.selected_year,
            temp2.mnth,
            temp2.lesson_type
    FROM
        temp2
    LEFT OUTER JOIN
        temp1
    ON
        temp1.lesson_year = temp2.selected_year
    AND
        temp1.lesson_month = temp2.mnth
    AND
        temp1.lesson_type=temp2.lesson_type
    GROUP BY temp2.selected_year, temp2.mnth, temp2.lesson_type
), lessons_per_month as(
    SELECT sum(t.c) as given_lessons, t.mnth, t.selected_year from lessons_per_month_by_type as t GROUP BY t.mnth, t.selected_year
), lessons_average_per_year as(
    SELECT AVG(t.given_lessons),t.selected_year FROM lessons_per_month as t GROUP BY t.selected_year
)

SELECT AVG(t.given_lessons),t.selected_year FROM lessons_per_month as t GROUP BY t.selected_year;
/*Display given lessons per month :
  SELECT * FROM lessons_per_month ORDER BY mnth;*/
/*Display given lessons per month by type :
  SELECT * FROM lessons_per_month_by_type;*/
/*SELECT * FROM  lessons_per_month_by_type ORDER BY mnth;*/