drop table country if exists;

create table country (
  id integer,
  countryname varchar(32),
  countrycode varchar(2)
);

insert into country (id, countryname, countrycode) values(1,'Angola','AO');
insert into country (id, countryname, countrycode) values(2,'Afghanistan','AF');
insert into country (id, countryname, countrycode) values(3,'Albania','AL');
insert into country (id, countryname, countrycode) values(4,'Algeria','DZ');
insert into country (id, countryname, countrycode) values(5,'Andorra','AD');
insert into country (id, countryname, countrycode) values(6,'Anguilla','AI');
insert into country (id, countryname, countrycode) values(7,'Antigua and Barbuda','AG');