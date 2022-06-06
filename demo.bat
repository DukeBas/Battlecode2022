@echo off 
set list=chalice charge chessboard cobra collaboration colosseum deer definitely_not_league desert despair dodgeball eckleburg equals fire flowers fortress highway highway_redux intersection island_hopping jellyfish lotus maptestsmall maze nottestsmall nyancat octopus_game olympics one_river panda pillars planets progress rivers rugged sandwich snowflake snowflake_redux snowman spine squer stronghold tower treasure tunnels uncomfortable underground valley vault walls
(for %%a in (%list%) do (
   gradlew run -PteamA=Trainwreck -PteamB=Sprint1 -Pmaps=%%a | (
   ping 127.0.0.1 -n 8 & nircmd sendkeypress p)
   TIMEOUT /T 20
))%