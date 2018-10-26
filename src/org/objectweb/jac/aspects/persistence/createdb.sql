--
-- PostgreSQL database creation script
--

CREATE SEQUENCE "object_id";
CREATE SEQUENCE "name_counter";

-- STORAGE table
-- Contains meta information about the storage
CREATE TABLE "storage" (
	"key" character varying PRIMARY KEY,
	"value" character varying
);

-- ROOTS table
-- Contains (id,jac_name) bindings
CREATE TABLE "roots" (
	"id" integer PRIMARY KEY,
	"name" character varying
);
CREATE UNIQUE INDEX "root_name" on "roots" ("name");

-- CLASSES table
-- Contains id -> classname mapping
CREATE TABLE "classes" (
	"id" integer PRIMARY KEY,
	"classid" character varying
);

-- OBJECTS table
-- Contains objects data (fields and references)
CREATE TABLE "objects" (
	"id" integer NOT NULL,
	"fieldid" character varying,
	"value" character varying
);
CREATE INDEX "objects_id" ON "objects" ("id");
CREATE UNIQUE INDEX "objects_field" ON "objects" ("id","fieldid");

-- SETS table
-- Contains set collections
CREATE TABLE "sets" (
       "id" integer NOT NULL,
       "value" character varying
);
CREATE INDEX "sets_id" ON "sets" ("id");
CREATE UNIQUE INDEX "sets_value" ON "sets" ("id","value");

-- LISTS table
-- Contains list collections
CREATE TABLE "lists" (
       "id" integer NOT NULL,
       "index"  integer NOT NULL,
       "value" character varying
);
CREATE INDEX "lists_id" ON "lists" ("id");
CREATE UNIQUE INDEX "lists_index" ON "lists" ("id","index");
CREATE INDEX "lists_value" ON "lists" ("id","value");

-- MAPS table
-- Contains map collections (hashtables)
CREATE TABLE "maps" (
       "id" integer NOT NULL,
       "key"  character varying,
       "value" character varying
);
CREATE INDEX "maps_id" ON "maps" ("id");
CREATE UNIQUE INDEX "maps_key" ON "maps" ("id","key");
CREATE INDEX "maps_value" ON "maps" ("id","value");
