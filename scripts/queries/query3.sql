/* List all instructors who has given more than a specific 
   number of lessons during the current month. Sum all lessons, 
   independent of type, and sort the result by the number of 
   given lessons. This query will be used to find instructors 
   risking to work too much, and will be executed daily. */

SELECT COUNT(instructor_id), instructor_id
FROM (SELECT * 
	  FROM music_lesson 
	  WHERE extract(month FROM appointed_time) = extract(month FROM now())  AND extract(year FROM appointed_time) = extract(year FROM now())) as t
GROUP BY instructor_id
HAVING COUNT(instructor_id) >= 2
ORDER BY count DESC