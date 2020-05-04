# PairProgramming_IntelliJ_plugin
this repository holds the code for a plugin for Jetbrains' IntelliJ which facilitates pair programming, 
by replicating what's done in one instance in an other and passing those changes through a central server
which hands them off to other parties involved in a pub-sub fashion.

What's duplicated:
- file renames, moves, copies (even from outside project), deletions, creations
- active content changes through the editor

file action filters:
- on maximum file-size (still needs to be determined which actions would be allowable)
- on path using regular expressions

all connected parties can change at any time and said changes are replicated to the others.

A rudimentary server can currently be found at: https://github.com/haroldbrinkhof/PairProgramming-routing-server
better manageable and secured one to come later.
