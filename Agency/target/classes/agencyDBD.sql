DROP TABLE APP.Agents;
DROP TABLE APP.Missions;

CREATE TABLE APP.Missions (
	id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                 "name" VARCHAR(255) NOT NULL,
	beginDate DATE NOT NULL,
	endDate DATE NOT NULL,
	difficulty INTEGER CONSTRAINT difficultyConstraint check (difficulty >= 1 and difficulty <= 10),
	capacity INTEGER CONSTRAINT capacityConstraint check (capacity >= 1),
	note VARCHAR(255)
);

CREATE TABLE APP.Agents (
	id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
	"name" VARCHAR(255) NOT NULL,
	born DATE NOT NULL,
	level INTEGER CONSTRAINT levelConstraint check (level >= 1 and level <= 10),
	note VARCHAR(255),
                 missionId BIGINT REFERENCES APP.Missions (ID) ON DELETE SET NULL
);
