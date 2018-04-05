package com.quickbite.rx2020.gui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.quickbite.rx2020.TextGame
import com.quickbite.rx2020.managers.SupplyManager
import com.quickbite.rx2020.managers.TradeManager

object TradeWindowGUI {
    private val tradeWindowTable = Table()
    private val tradeSliderWindow = Table()
    private lateinit var settingsButton:ImageButton

    val isOpen:Boolean
        get() = tradeWindowTable.parent != null

    fun buildTradeWindow():Table{
        //TODO Maybe this should be in its own class?
        tradeWindowTable.clear()
        tradeWindowTable.background = TextureRegionDrawable(TextureRegion(TextGame.manager.get("TradeWindow2", Texture::class.java)))
        tradeWindowTable.setSize(450f, 400f)

        val labelTable: Table = Table()
        val listTable: Table = Table()
        val offerTable: Table = Table()

        val labelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        val amtLabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        val giveButtonStyle = ImageButton.ImageButtonStyle()
        giveButtonStyle.imageUp = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("nextButtonWhite"))

        val takeButtonStyle = ImageButton.ImageButtonStyle()
        takeButtonStyle.imageUp = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("nextButtonWhiteLeft"))

        val textButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.font = TextGame.manager.get("spaceFont2", BitmapFont::class.java)
        textButtonStyle.fontColor = Color.WHITE

        val exomerLabel = Label("Exomer751", labelStyle)
        exomerLabel.setFontScale(0.15f)
        exomerLabel.setAlignment(Align.center)

        val nativeLabel = Label("Natives", labelStyle)
        nativeLabel.setFontScale(0.15f)
        nativeLabel.setAlignment(Align.center)

        val yourOfferLabel = Label("Your Offer:", labelStyle)
        val yourOfferAmtLabel = Label("0", labelStyle)
        yourOfferLabel.setFontScale(0.15f)
        yourOfferAmtLabel.setFontScale(0.15f)
        yourOfferAmtLabel.setAlignment(Align.center)
        yourOfferAmtLabel.color = Color.GREEN

        val otherOfferAmtLabel = Label("0", labelStyle)
        val otherOfferLabel = Label(":Their Offer", labelStyle)
        otherOfferAmtLabel.setFontScale(0.15f)
        otherOfferAmtLabel.setAlignment(Align.center)
        otherOfferAmtLabel.color = Color.RED
        otherOfferLabel.setFontScale(0.15f)

        TradeManager.generateLists()

        val amountLabel = Label("Amt", labelStyle)
        amountLabel.setFontScale(0.15f)
        amountLabel.setAlignment(Align.center)

        val otherAmountLabel = Label("Amt", labelStyle)
        otherAmountLabel.setFontScale(0.15f)
        otherAmountLabel.setAlignment(Align.center)

        val nameLabel = Label("Name", labelStyle)
        nameLabel.setFontScale(0.15f)
        nameLabel.setAlignment(Align.center)

        val otherNameLabel = Label("Name", labelStyle)
        otherNameLabel.setFontScale(0.15f)
        otherNameLabel.setAlignment(Align.center)

        val valueLabel = Label("Worth", labelStyle)
        valueLabel.setFontScale(0.15f)
        valueLabel.setAlignment(Align.center)

        val otherValueLabel = Label("Cost", labelStyle)
        otherValueLabel.setFontScale(0.15f)
        otherValueLabel.setAlignment(Align.center)

        val tradeTitleLabel = Label("Trade", labelStyle)
        tradeTitleLabel.setFontScale(0.15f)
        tradeTitleLabel.setAlignment(Align.center)

        val acceptButton = TextButton("Accept", textButtonStyle)
        acceptButton.label.setFontScale(0.15f)

        val spaceX = 4f

        listTable.add(valueLabel).width(60f).spaceRight(spaceX).height(24f)
        listTable.add(nameLabel).width(76f).spaceRight(spaceX)
        listTable.add(amountLabel).width(44f).spaceRight(spaceX)

        listTable.add(tradeTitleLabel).width(58f).spaceRight(spaceX).colspan(3)

        listTable.add(otherAmountLabel).width(44f).spaceRight(spaceX)
        listTable.add(otherNameLabel).width(76f).spaceRight(spaceX)
        listTable.add(otherValueLabel).width(60f)

        listTable.row().spaceTop(5f)

        //The item list.
        val exomerList = TradeManager.exomerList
        val otherList = TradeManager.otherList

        val rightArrow = TextGame.smallGuiAtlas.findRegion("nextButtonWhite")
        val leftArrow = TextGame.smallGuiAtlas.findRegion("nextButtonWhiteLeft")

        //For each item....
        for(i in exomerList!!.indices){
            val exItem = exomerList[i]
            val otherItem = otherList!![i]

            //Exomer item name
            val exomerItemNameLabel = Label(exItem.abbrName, labelStyle)
            exomerItemNameLabel.setFontScale(0.13f)
            exomerItemNameLabel.setAlignment(Align.center)

            //Exomer amount
            val exomerItemAmountLabel = Label(exItem.amt.toInt().toString(), labelStyle)
            exomerItemAmountLabel.setFontScale(0.13f)
            exomerItemAmountLabel.setAlignment(Align.center)
//            exomerItemAmountLabel.color = Color.SKY

            //Exomer worth
            val exomerItemValueLabel = Label(exItem.worth.toString(), labelStyle)
            exomerItemValueLabel.setFontScale(0.13f)
            exomerItemValueLabel.setAlignment(Align.center)
            exomerItemValueLabel.color = Color.GREEN

            //Other name
            val nativeItemNameLabel = Label(otherItem.abbrName, labelStyle)
            nativeItemNameLabel.setFontScale(0.13f)
            nativeItemNameLabel.setAlignment(Align.center)

            //Other amt
            val nativeItemAmountLabel = Label(otherItem.amt.toInt().toString(), labelStyle)
            nativeItemAmountLabel.setFontScale(0.13f)
            nativeItemAmountLabel.setAlignment(Align.center)
//            nativeItemAmountLabel.color = Color.ORANGE

            //Other worth
            val nativeItemValueLabel = Label(otherItem.worth.toString(), labelStyle)
            nativeItemValueLabel.setFontScale(0.13f)
            nativeItemValueLabel.setAlignment(Align.center)
            nativeItemValueLabel.color = Color.RED

            //Amt traded
            val amtLabel = Label("0", amtLabelStyle)
            amtLabel.setFontScale(0.13f)
            amtLabel.setAlignment(Align.center)

            //Add the amount then name to the left table.
            listTable.add(exomerItemValueLabel).left().fillX().spaceRight(spaceX).spaceBottom(spaceX).height(24f)
            listTable.add(exomerItemNameLabel).fillX().spaceRight(spaceX).spaceBottom(spaceX)
            listTable.add(exomerItemAmountLabel).fillX().spaceRight(spaceX).spaceBottom(spaceX)

            //Add the stuff to the center table.
            val leftArrowCell: Cell<Actor> = listTable.add().size(16f, 16f).spaceBottom(spaceX)
            listTable.add(amtLabel).space(0f, 0f, 0f, 0f).width(26f).spaceBottom(spaceX)
            val rightArrowCell: Cell<Actor> = listTable.add().size(16f, 16f).spaceRight(spaceX).spaceBottom(spaceX)

            //Add the name then amount to the right table.
            listTable.add(nativeItemAmountLabel).fillX().spaceRight(spaceX).spaceBottom(spaceX)
            listTable.add(nativeItemNameLabel).fillX().spaceRight(spaceX).spaceBottom(spaceX)
            listTable.add(nativeItemValueLabel).right().fillX().spaceBottom(spaceX)

            val rightArrowImage: Image = Image(rightArrow)
//            rightArrowImage.color = Color.ORANGE

            val leftArrowImage: Image = Image(leftArrow)
//            leftArrowImage.color = Color.SKY

            val func = { newAmt: Int ->
                var tradeAmt = amtLabel.text.toString().toInt()
                var yourOffer = yourOfferAmtLabel.text.toString().toInt()
                var otherOffer = otherOfferAmtLabel.text.toString().toInt()

                //If the trade amount is positive and change amount is negative.
                if(tradeAmt > 0 && newAmt < 0){
                    val toZero = tradeAmt

                    yourOffer -= exItem.worth* newAmt
                    otherOffer -= otherItem.worth*toZero
                    if(otherOffer <= 0) otherOffer = 0

                    //If trade amount is negative and change amount is positive.
                }else if(tradeAmt < 0 && newAmt > 0){
                    val toZero = tradeAmt

                    yourOffer += exItem.worth*toZero
                    otherOffer += otherItem.worth* newAmt
                    if(otherOffer <= 0) otherOffer = 0

                }else if(tradeAmt >= 0 && newAmt >= 0){
                    otherOffer += otherItem.worth*(newAmt - tradeAmt)
                    if(otherOffer <=0) otherOffer = 0
                    //otherOffer -= otherItem.worth*(changeAmt - tradeAmt)
                }else if(tradeAmt <= 0 && newAmt <= 0){
                    //yourOffer -= exItem.worth*(changeAmt - tradeAmt)
                    yourOffer -= exItem.worth*(newAmt - tradeAmt)
                    if(yourOffer <= 0) yourOffer = 0
                }

                //Change the amt that we got from the text.
                tradeAmt = newAmt

                //Set the exomer and other item amount label.
                exomerItemAmountLabel.setText(exItem.amt.toInt().toString())
                nativeItemAmountLabel.setText(otherItem.amt.toInt().toString())

                //If amt is positive, make it green. Negative, make it red. 0, make it white.
                when {
                    tradeAmt > 0 -> {amtLabel.color = Color.GREEN; leftArrowCell.setActor(leftArrowImage); rightArrowCell.clearActor()}
                    tradeAmt < 0 -> {amtLabel.color = Color.RED; rightArrowCell.setActor(rightArrowImage); leftArrowCell.clearActor()}
                    else -> {amtLabel.color = Color.WHITE; leftArrowCell.clearActor(); rightArrowCell.clearActor()}
                }

                when {
                    yourOffer < otherOffer -> acceptButton.label.color = Color.RED
                    else -> acceptButton.label.color = Color.WHITE
                }

                //Change the offer amounts.
                amtLabel.setText(tradeAmt.toString())
                yourOfferAmtLabel.setText(yourOffer.toString())
                otherOfferAmtLabel.setText(otherOffer.toString())
            }

            amtLabel.addListener(object: ClickListener(){
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    openTradeSlider(exomerList[i], otherList[i], func)
                    super.clicked(event, x, y)
                }
            })

            listTable.row()
        }

        acceptButton.addListener(object: ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                val yourOffer = yourOfferAmtLabel.text.toString().toInt()
                val theirOffer = otherOfferAmtLabel.text.toString().toInt()
                if(yourOffer >= theirOffer){
                    for(item in exomerList)
                        SupplyManager.setSupplyAmount(item.name, item.currAmt)

                    //TODO Supplies don't update?
                    GameScreenGUIManager.updateSuppliesGUI()
                    GameScreenGUIManager.closeTradeWindow()
                }
            }
        })

        //Add stuff to the offer (your/their offer) table
        offerTable.add(yourOfferLabel).right().padRight(5f)
        offerTable.add(yourOfferAmtLabel).right().spaceRight(30f).width(25f).height(25f)
        offerTable.add()
        offerTable.add(otherOfferAmtLabel).left().padRight(5f).spaceLeft(30f).width(25f).height(25f)
        offerTable.add(otherOfferLabel).left()

        //The titles table
        labelTable.add(exomerLabel).width(215f).height(30f)
        labelTable.add(nativeLabel).width(215f).height(30f)
        labelTable.top()

        // Add all the tables to the main table.
        tradeWindowTable.add(labelTable).fillX().expandX().pad(0f, 10f, 4f, 10f).top().height(30f)
        tradeWindowTable.row()
        tradeWindowTable.add(listTable).pad(0f, 5f, 5f, 5f).top()
        tradeWindowTable.row()
        tradeWindowTable.add(offerTable).fillX().expandX().top().spaceBottom(0f)
        tradeWindowTable.row()
        tradeWindowTable.add(acceptButton).fill().expand().padBottom(4f)

        tradeWindowTable.setPosition(TextGame.viewport.worldWidth/2f - tradeWindowTable.width/2f, TextGame.viewport.worldHeight/2f - tradeWindowTable.height/2f)

        return tradeWindowTable
    }

    /**
     * Opens the slider for a particular trade item.
     */
    private fun openTradeSlider(exItem: TradeManager.TradeSupply, oItem: TradeManager.TradeSupply, callback:(Int)->Unit){
        tradeSliderWindow.clear()
        tradeSliderWindow.background = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("pixelBlack"))
        tradeSliderWindow.setSize(300f, 100f)
        tradeSliderWindow.setPosition(TextGame.viewport.worldWidth/2f - 300f/2f, TextGame.viewport.worldHeight/2f - 100f/2f)

        val buttonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = TextGame.manager.get("spaceFont2", BitmapFont::class.java)
        buttonStyle.fontColor = Color.WHITE
//        buttonStyle.up = TextureRegionDrawable(TextureRegion(TextGame.manager.get("", Texture::class.java)))

        val labelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        val sliderStyle = Slider.SliderStyle()
        sliderStyle.background = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("sliderWhite"))
        sliderStyle.knob = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("sliderKnob"))

        val tradeSlider = Slider(-exItem.amt, oItem.amt, 1f, false, sliderStyle)
        tradeSlider.value = 0f

        val currLabel = Label("0", labelStyle)
        currLabel.setFontScale(0.15f)
        currLabel.setAlignment(Align.center)

        val minLabel = Label((-exItem.amt).toInt().toString(), labelStyle)
        minLabel.setFontScale(0.15f)
        minLabel.setAlignment(Align.center)

        val maxLabel = Label(oItem.amt.toInt().toString(), labelStyle)
        maxLabel.setFontScale(0.15f)
        maxLabel.setAlignment(Align.center)

        val lessButton = TextButton("-", buttonStyle)
        lessButton.label.setFontScale(0.17f)
        lessButton.label.setAlignment(Align.center)

        val moreButton = TextButton("+", buttonStyle)
        moreButton.label.setFontScale(0.17f)
        moreButton.label.setAlignment(Align.center)

        val acceptButton = TextButton("Accept", buttonStyle)
        acceptButton.label.setFontScale(0.15f)
        acceptButton.label.setAlignment(Align.center)

        val cancelButton = TextButton("Cancel", buttonStyle)
        cancelButton.label.setFontScale(0.15f)
        cancelButton.label.setAlignment(Align.center)

        val buttonTable = Table()
        buttonTable.add(acceptButton).spaceRight(20f)
        buttonTable.add(cancelButton)

        tradeSliderWindow.add(lessButton)
        tradeSliderWindow.add(currLabel)
        tradeSliderWindow.add(moreButton)

        tradeSliderWindow.row()

        tradeSliderWindow.add(minLabel).spaceRight(5f)
        tradeSliderWindow.add(tradeSlider).spaceRight(5f)
        tradeSliderWindow.add(maxLabel)

        tradeSliderWindow.row()

        tradeSliderWindow.add(buttonTable).colspan(3).spaceBottom(10f)

        tradeSlider.addListener(object: ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                currLabel.setText(tradeSlider.value.toInt().toString())
                callback(tradeSlider.value.toInt())
                exItem.currAmt = exItem.amt + tradeSlider.value.toInt()
            }
        })

        moreButton.addListener(object: ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                tradeSlider.value += 1
            }
        })

        lessButton.addListener(object: ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                tradeSlider.value -= 1
            }
        })

        acceptButton.addListener(object: ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                closeTradeSlider()
            }
        })

        cancelButton.addListener(object: ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                callback(0)
                closeTradeSlider()
            }
        })

        TextGame.stage.addActor(tradeSliderWindow)
    }

    /**
     * Closes just the slider window
     */
    fun closeTradeSlider() = tradeSliderWindow.remove()

    /**
     * Closes the entire trade window
     */
    fun closeTradeWindow(){
        tradeSliderWindow.remove()
        tradeWindowTable.remove()
    }
}