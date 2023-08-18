CREATE ROLE pgcdc WITH LOGIN PASSWORD 'testpass';
ALTER ROLE pgcdc REPLICATION;
ALTER ROLE pgcdc CREATEDB;

CREATE DATABASE cdctest

drop table students;
drop table student_admissions;

create table students (
	id integer primary key,
	name VARCHAR(255)
);


create table student_admissions (
	id integer primary key,
	student_id integer references students(id),
	standard VARCHAR(255)
);


insert into students values (1, 'Dinesh');
insert into student_admissions values (1, 1, '8');

insert into students values (2, 'Ak');
insert into student_admissions values (1, 1, '8');

begin;
update students set name = 'dinsaw' where id = 1;
update students set name = 'ab' where id = 2;
update student_admissions set standard = '10' where id =1;
commit;