Create table department(id serial primary key, name varchar(255) not null);
Create table employee(id serial primary key , name varchar(255), salary varchar(255) not null, department_id smallint not null)