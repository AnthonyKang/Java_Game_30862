#THis script will compile and run the game
rm -rf classes

mkdir classes

javac -d classes/ @sources.txt

java -cp classes/ com.brackeen.javagamebook.tilegame.GameManager


