package com.example.kotlingame

class Map(private var n: Int, private var m: Int){
    lateinit var theMap : Array<Array<Ceil>>
    private var countOfStones = 5
    private var countOfCoins = 5
    private var countOfBombs = 5
    private lateinit var heroPos: Pair<Int,Int>
    fun setParams(stones:Int = countOfStones, coins:Int = countOfCoins, bombs:Int = countOfBombs) {
        countOfStones = stones
        countOfBombs = bombs
        countOfCoins = coins
        generateRandomMap()
    }

    fun getMap(): Array<Array<Ceil>>{
        return theMap
    }
    fun getHeroPos() : Pair<Int,Int> {
        return heroPos
    }
    companion object{

        enum class Ceil {//клетка поля
        HERO,
            BARRIER,
            VOID,
            BOMB,
            END,
            COIN;

        }

    }



    private fun setBitMapRandom(name: Ceil){//случайно ставим клетку на карте
        while(true) {
            val i = (0 until n).random()
            val j = (0 until m).random()
            if(theMap[i][j] == Ceil.VOID) {
                if(name == Ceil.HERO) heroPos = Pair(i,j)
                theMap[i][j] = name
                return
            }
        }
    }

    fun generateRandomMap(){

        theMap = Array(n) { Array(m) {Ceil.VOID}}

        setBitMapRandom(Ceil.END)
        setBitMapRandom(Ceil.HERO)
        for(i in 0 until countOfBombs)
            setBitMapRandom(Ceil.BOMB)
        for(i in 0 until countOfCoins)
            setBitMapRandom(Ceil.COIN)
        for(i in 0 until countOfStones)
            setBitMapRandom(Ceil.BARRIER)

    }
    init{
        generateRandomMap()
    }

}