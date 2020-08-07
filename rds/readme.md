# RDS Notes

## psql

Run a container to get psql tools and login to database:
```
$ docker run --rm -it postgres /bin/bash
// in container
# psql "dbname=postgres host=dr1jd4vs5n5j845.cfnmvj1tns1k.us-east-1.rds.amazonaws.com user=dbmsowner port=5432 password=G1l1p1g0s"
psql (12.3 (Debian 12.3-1.pgdg100+1), server 9.6.6)
SSL connection (protocol: TLSv1.2, cipher: ECDHE-RSA-AES256-GCM-SHA384, bits: 256, compression: off)
Type "help" for help.

postgres=>

// list databases
postgres=> \l

// switch databases
postgres=> \c dev-mkt-template-docker
psql (12.3 (Debian 12.3-1.pgdg100+1), server 9.6.6)
SSL connection (protocol: TLSv1.2, cipher: ECDHE-RSA-AES256-GCM-SHA384, bits: 256, compression: off)
You are now connected to database "dev-mkt-template-docker" as user "dbmsowner".

// list tables
dev-mkt-template-docker=> \dt
                      List of relations
 Schema |         Name          | Type  |        Owner
--------+-----------------------+-------+---------------------
 public | customer              | table | mkt-template-docker
 public | databasechangelog     | table | mkt-template-docker
 public | databasechangeloglock | table | mkt-template-docker
 public | template_user         | table | mkt-template-docker
 public | widget                | table | mkt-template-docker

 // switch back to postgres, get list of users
 postgres=> \c postgres
 postgres=> select usename from pg_user;
                              usename
 -----------------------------------------------------------------
  mkt-design-mediaprocessor
  mkt-taskmonitor
  mkt-design
  mkt-datahub-eximdata
  mkt-channels-taskmonitor
  mkt-analytic-abt
  mkt-datahub-segment
  mkt-plan-assets
  ...

postgres=> select count(*) from pg_user;
 count
-------
    66

// database statistics
postgres=> select datname, tup_fetched, tup_inserted, tup_updated, conflicts, deadlocks, blk_read_time, blk_write_time, stats_reset from pg_stat_database;

// table stats
postgres=> select schemaname, relname, seq_scan, idx_scan, last_vacuum, last_autovacuum  from pg_stat_all_tables;
     schemaname     |         relname         | seq_scan  |  idx_scan  |         last_vacuum         |        last_autovacuum
--------------------+-------------------------+-----------+------------+-----------------------------+-------------------------------
 information_schema | sql_sizing              |         4 |            |                             | 2020-03-06 08:02:12.070864+00
 pg_catalog         | pg_auth_members         |       145 |    9046582 |                             | 2020-05-13 08:16:57.159604+00
 pg_catalog         | pg_operator             |        16 |     504985 |                             | 2020-03-06 08:01:41.130887+00
 public             | databasechangeloglock   |        83 |          9 |                             | 2020-01-25 23:02:43.134305+00
 ...
```

Track queries / connections to the database (pg_stat_activity):
* datname : database name the query connects to
* usename : name of user logged into backend
* client_addr: IP of client
* backend_start : time when client connected to the server
* query_start : time when currently active query was started, or if state is not active, when the last query was started
* state_change : time when state was last changed
* wait_event_type : backend is waiting on something (Lock, BufferPin, ...)
* wait_event : description
* state : active, idle, ...
```
// take a look at all connections
postgres=> select datname, usename, client_addr, backend_start, query_start, state_change, wait_event_type, wait_event, state from pg_stat_activity;

// connections per user
select usename, count(*) as cnt from pg_stat_activity group by usename order by cnt desc;

select datname, count(*) as cnt from pg_stat_activity group by datname order by cnt desc;

// number of idle connections
select usename, count(*) as num_idle_connections from pg_stat_activity where state='idle' group by usename order by num_idle_connections desc;

select usename, count(*) as num_active_connections from pg_stat_activity where state='active' group by usename order by num_active_connections desc;

postgres=> select distinct state, count(*) as count from pg_stat_activity group by state;
        state        | count
---------------------+-------
                     |     1
 idle                |  1083
 active              |     2
 idle in transaction |     4
(4 rows)

```
