drop database if exists searchdb2;
create database searchdb2;
use searchdb2;

drop table if exists doc_id_url;
create table doc_id_url (
	id varchar(10) not null,
    url varchar (3000) not null,
    primary key (id)
);

drop table if exists terms;
create table terms(
	term varchar (100) not null,
    primary key (term)
);

drop table if exists term_id_tf_tfidf;
create table term_id_tf_tfidf(
	term_id varchar (100) not null,
    doc_id varchar(10) not null,
    tf int null,
    tfidf float(20) null,
    foreign key (term_id) references terms(term),
    foreign key (doc_id) references doc_id_url(id)
    
);

-- drop table if exists term_doc;
-- create table term_doc(
-- 	term_id varchar(10) not null,
--     doc_id varchar (10) not null,
--     foreign key (term_id) references term(term),
--     foreign key (doc_id) references doc_id_url(id)
-- );
-- 


