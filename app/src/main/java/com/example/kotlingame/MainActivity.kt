package com.example.kotlingame

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


class MainActivity : ComponentActivity() {
    var coinsScore = 0
    var loseGames = 0
    var winGames = 0

    lateinit var swipeDetector : SwipeDetector
    lateinit var map : Map
    lateinit var mapCeils : Array<Array<Map.Companion.Ceil>>
    private var n = 10
    private var m = 10
    lateinit var heroPos: Pair<Int,Int>

    class modeButton(val name: String, val mode: Mode)
    private var mode = Mode.GO
    val modeButtons = listOf(modeButton("Jump", Mode.JUMP), modeButton("Go",Mode.GO), modeButton("Break", Mode.BREAK))


    enum class Mode{
        GO,
        BREAK,
        JUMP
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        //setContentView(R.layout.activity_main)

        swipeManager()
        mapManager()

        ShowMap()
    }


    sealed class Move{
        class Position (val pos: Pair<Int, Int>) : Move()
        class MoveDelta(val pos: Pair<Int, Int>, val vector: Pair<Int, Int>) : Move()
        class MoveJump (val pos: Pair<Int, Int>, val vector: Pair<Int, Int>) : Move() // moveCount сколько раз может перепрыгнуть камень(в моей версии игры 1)
        class MoveBreak(val pos: Pair<Int, Int>, val vector: Pair<Int, Int>, val breakCount: Int): Move()
    }
    fun isInsideMap(pos: Pair<Int, Int>) : Boolean{
        return if(pos.first in 0 until n && pos.second in 0 until m) true else false
    }
    fun isBarrier(pos: Pair<Int, Int>) : Boolean {
        return isInsideMap(pos) && mapCeils[pos.first][pos.second] == Map.Companion.Ceil.BARRIER
    }

    private fun endGame(youWin: Boolean) {
        if(youWin) ++winGames
        else ++loseGames
        restartGame()

    }
    private fun interaction(pos: Pair<Int,Int>) {

        when(mapCeils[pos.first][pos.second]) {
            Map.Companion.Ceil.COIN -> {
                ++coinsScore
                Log.v("Coins", "$coinsScore")
            }
            Map.Companion.Ceil.BOMB ->{
                endGame(false)
            }
            Map.Companion.Ceil.END ->{
                endGame(true)
            }
            else -> {}
        }
        mapCeils[pos.first][pos.second] = Map.Companion.Ceil.VOID
    }

    private fun restartGame(){
        mapManager()
        ShowMap()
    }
    private fun NextHeroPosition(move: Move) : Pair<Int,Int> {
        when(move) {
            is Move.Position -> {
                interaction(move.pos)
                return move.pos
            }

            is Move.MoveDelta -> { //просто сдвиг в сторону если возможно
                interaction(move.pos)
                val x = move.pos.first + move.vector.first
                val y = move.pos.second + move.vector.second
                if(isInsideMap(Pair(x,y))) {
                    if(mapCeils[x][y] == Map.Companion.Ceil.BARRIER)
                        return NextHeroPosition(Move.Position(move.pos))
                    else
                        return NextHeroPosition(Move.MoveDelta(Pair(x,y), move.vector))
                } else {
                    return NextHeroPosition(Move.Position(move.pos))
                }
            }
            is Move.MoveJump -> {
                interaction(move.pos)
                val x = move.pos.first + move.vector.first
                val y = move.pos.second + move.vector.second
                if(isInsideMap(Pair(x,y))) {
                    if(mapCeils[x][y] in listOf(Map.Companion.Ceil.BARRIER, Map.Companion.Ceil.BOMB)) {
                        val jumpx = x + move.vector.first
                        val jumpy = y + move.vector.second
                        if(isInsideMap(Pair(jumpx, jumpy)) && !isBarrier(Pair(jumpx, jumpy))) {
                            return NextHeroPosition(Move.Position(Pair(jumpx,jumpy)))
                        } else {
                            if(mapCeils[x][y] == Map.Companion.Ceil.BOMB)
                                return NextHeroPosition(Move.Position(Pair(x,y)))
                            else
                                return NextHeroPosition(Move.Position(move.pos))
                        }
                    } else {
                        return NextHeroPosition(Move.MoveJump(Pair(x,y), move.vector ))
                    }
                } else {
                    return NextHeroPosition(Move.Position(move.pos))
                }
            }
            is Move.MoveBreak -> {
                interaction(move.pos)
                val x = move.pos.first + move.vector.first
                val y = move.pos.second + move.vector.second
                val breaker = move.breakCount
                if(isInsideMap(Pair(x,y))) {
                    if(isBarrier(Pair(x,y))) {
                        if(breaker > 1) {
                            return NextHeroPosition(Move.MoveBreak(Pair(x,y), move.vector, breaker - 1))
                        } else if(breaker == 1){
                            return NextHeroPosition(Move.MoveDelta(Pair(x,y), move.vector))
                        } else {
                            return NextHeroPosition(Move.Position(Pair(x,y)))
                        }
                    } else {
                        return NextHeroPosition(Move.MoveBreak(Pair(x,y), move.vector, breaker))
                    }
                } else {
                    return NextHeroPosition(Move.Position(move.pos))
                }
            }

        }
    }


    private fun move(dx:Int, dy:Int) {
        mapCeils[heroPos.first][heroPos.second] = Map.Companion.Ceil.VOID

        when(mode){
            Mode.GO -> heroPos = NextHeroPosition(Move.MoveDelta(heroPos, Pair(dx,dy)))
            Mode.JUMP->heroPos = NextHeroPosition(Move.MoveJump(heroPos,Pair(dx,dy)))
            Mode.BREAK->heroPos=NextHeroPosition(Move.MoveBreak(heroPos,Pair(dx,dy), 1))
        }
        //heroPos = NextHeroPosition(Move.MoveDelta(heroPos, Pair(dx,dy)))
        mapCeils[heroPos.first][heroPos.second] = Map.Companion.Ceil.HERO
    }

    private fun mapManager(){
        map = Map(n,m)
        map.setParams(50,10,5)
        mapCeils = map.getMap()
        heroPos = map.getHeroPos()
    }

    private fun ShowMap(){
        setContent{
            ShowMapImages()
            ShowInterface()
        }

    }
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return swipeDetector.onTouchEvent(event!!)
    }

    private fun swipeManager(){
        swipeDetector = object : SwipeDetector(ViewConfiguration.get(this).scaledTouchSlop) {
            override fun onSwipeDetected(direction: Direction) {
                when (direction) {
                    Direction.UP -> {move(-1,0)}
                    Direction.DOWN -> {move(1,0)}
                    Direction.RIGHT -> {move(0,1)}
                    Direction.LEFT -> {move(0,-1)}
                    Direction.UNEXPECTED -> {}
                }
                Log.v("Swipe", direction.toString())
                ShowMap()
            }
        }
    }

    @Composable
    fun ShowText(){
        Box(//Mode
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.25f)
        ) {
            Text(text = mode.toString(), fontSize = 30.sp)
        }
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.1f)
        ) {
            Text(text = "You win ${winGames.toString()}", fontSize = 20.sp)
            Text(text = "You have ${coinsScore.toString()} coins.", fontSize = 20.sp)
            Text(text = "You lose ${loseGames.toString()}", fontSize = 20.sp)

        }


    }
    @Composable
    fun ShowButtons(){
        Box(//restart button
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.90f)
        ){
            Button(
                onClick = ({
                    restartGame()
                })
                //modifier = Modifier.fillMaxHeight(0.1f).fillMaxWidth()
            ) {
                Text(text = "Next level", fontSize = 25.sp, color = Color.Cyan)
            }
        }


        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.fillMaxHeight(0.8f).fillMaxWidth()
        ){
            for(i in modeButtons){
                Button(
                    onClick = ({
                        mode = i.mode
                        ShowMap()
                    })
                ){
                    Text(i.name, fontSize = 25.sp)
                }
            }


        }
    }
    @Composable
    fun ShowInterface(){
        ShowText()
        ShowButtons()
    }


    @Composable
    fun ShowMapImages() {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxHeight()
        ) {
            for(i in 0 until n)
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ){
                    for(j in 0 until m) {
                        when(mapCeils[i][j]) {
                            Map.Companion.Ceil.BARRIER-> CreateImage(R.drawable.stone, "stone")
                            Map.Companion.Ceil.END ->  CreateImage(R.drawable.`out`, "end")
                            Map.Companion.Ceil.BOMB -> CreateImage(R.drawable.bomb, "bomb")
                            Map.Companion.Ceil.COIN -> CreateImage(R.drawable.coin, "Coin")
                            Map.Companion.Ceil.HERO -> CreateImage(R.drawable.ball, "ballHero")
                            Map.Companion.Ceil.VOID -> CreateImage(R.drawable.nothing, "Void")
                        }
                    }
                }
        }


    }
}

@Composable
fun CreateImage(name: Int, description: String) {
    Image(
        bitmap = ImageBitmap.imageResource(name),
        contentDescription = description,
        modifier = Modifier.border(1.dp,Color.Black)
    )
}
