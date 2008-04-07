Story: Empty spaces with three neighbours create a cell

As a game producer
I want empty cells with three neighbours to die
So that I have a minimum feature set to ship

Scenario: the glider

Given the grid looks like
...X..
..X...
..XXX.
......
......
When the next step occurs
Then the grid should look like
......
..X.X.
..XX..
...X..
......
When the next step occurs
Then the grid should look like
......
..X...
..X.X.
..XX..
......
When the next step occurs
Then the grid should look like
......
...X..
.XX...
..XX..
......
When the next step occurs
Then the grid should look like
......
..X...
.X....
.XXX..
......
