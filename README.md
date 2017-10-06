# Deadman Switch

A deadman switch as a service.

#### Definitions

**Task**

A task represents some unit of work that must be completed before a specific 
deadline. If the task is not completed before the deadline, tje task expires;
causing expiration records to be written to a C* keyspace.

**Aggregate**

A high level grouping of tasks. Typically, an aggregate should contain no more than
1,000 tasks.

**Entity**

A secondary grouping of tasks within an aggregate. Typically, an aggregate should 
contain one or more entity groupings.

**Key**

A human readable identifier for a single task. The key must be unique within an entity
grouping.

**Tag**

A series of custom task identifiers. These can be used to arbitrarily query expired 
tasks within a time window (day, week, or month). Tags must be lower case and contain
no spaces or special characters.
