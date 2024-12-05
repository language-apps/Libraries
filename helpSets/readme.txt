To get help to work first update the map.jhm and the application.hs files. Next create 
toc.xml and index.xml. Be very careful, the programs give up with no error messages when the 
syntax is wrong.

Each folder with the help set files contains
1) acorns.hs - definition of the help set views
2) map.jhm - definition of target weg pages
3) index.xml and toc.xml - Definition of target and index views
4) jhindexer.jar - Jar files to create search view
5) JavaHelpSearch - folder with search database (after search view created)

To Create search view
1) For new help sets, make sure that makeSearchView.bat is updated (model the change after the others in that file)
2) Run the makeSearchView.bat file
3) To Test: run testSearchView.bat with the main directory of the helpSet as a command argument (e.g. LessonsPictures)
and type a query string (when done type . to exit).



 

