package controllers;

import java.awt.event.MouseEvent;

import anivarthi.AcesAndKings;
import ks.common.controller.SolitaireReleasedAdapter;
import ks.common.model.Card;
import ks.common.model.Column;
import ks.common.model.Move;
import ks.common.model.MultiDeck;
import ks.common.model.Pile;
import ks.common.view.CardView;
import ks.common.view.Container;
import ks.common.view.DeckView;
import ks.common.view.PileView;
import ks.common.view.Widget;
import move.ColumnToAceFoundationMove;
import move.PileToAceFoundationMove;

/**
 * This class is the controller for handling moves from and to any given ace
 * foundation.
 *
 * @author Aditya Nivarthi
 */
public class AceFoundationController extends SolitaireReleasedAdapter {

    final String RESERVE_STR = "reserve";

    PileView pileView;
    DeckView deck;

    /**
     * Constructor for AceFoundationController class.
     * 
     * @param theGame
     *            The AcesAndKings game.
     * @param foundation
     *            The ace foundation to be interacted with.
     */
    public AceFoundationController(AcesAndKings theGame, PileView foundation, DeckView deck) {
        super(theGame);
        pileView = foundation;
        this.deck = deck;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
     * 
     * Handles mouse pressed events on the ace views. Removes the card from the
     * view and enables the card for dragging.
     */
    @Override
    public void mousePressed(MouseEvent me) {
        Container c = theGame.getContainer();

        // Get the source of the card to drag
        Pile pile = (Pile) pileView.getModelElement();
        if (pile.count() == 0) {
            c.releaseDraggingObject();
            return;
        }

        // Get the card to drag
        CardView cardView = pileView.getCardViewForTopCard(me);

        if (cardView == null) {
            c.releaseDraggingObject();
            return;
        }

        // Set card for dragging
        Widget w = c.getActiveDraggingObject();
        if (w != Container.getNothingBeingDragged()) {
            System.err.println(
                    "AceFoundationController::mousePressed(): Unexpectedly encountered a dragging object during a mouse press.");
            return;
        }

        c.setActiveDraggingObject(cardView, me);
        c.setDragSource(pileView);
        pileView.redraw();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
     * 
     * Handles mouse released events on the ace views. Determines the move to be
     * made based on the source of the card and the ace view released on.
     */
    @Override
    public void mouseReleased(MouseEvent me) {
        Container c = theGame.getContainer();

        // Gets the currently dragged card
        Widget draggingWidget = c.getActiveDraggingObject();
        if (draggingWidget == Container.getNothingBeingDragged()) {
            System.err.println("AceFoundationController::mouseReleased() unexpectedly found nothing being dragged.");
            c.releaseDraggingObject();
            return;
        }

        // Get source location of dragged card
        Widget fromWidget = c.getDragSource();
        if (fromWidget == null) {
            System.err.println("AceFoundationController::mouseReleased(): somehow no drag source in container.");
            c.releaseDraggingObject();
            return;
        }

        // Handle source being a reserve.
        if (fromWidget.getModelElement().getName().contains(RESERVE_STR)) {
            Pile targetPile = (Pile) pileView.getModelElement();
            Column sourceColumn = (Column) fromWidget.getModelElement();

            CardView cardView = (CardView) draggingWidget;
            Card draggedCard = (Card) cardView.getModelElement();
            Move move = new ColumnToAceFoundationMove(sourceColumn, draggedCard, targetPile);

            if (move.doMove(theGame)) {
                theGame.pushMove(move);
                theGame.refreshWidgets();
            } else {
                fromWidget.returnWidget(draggingWidget);
            }
        } else {
            // Handle source being a pile.
            Pile targetPile = (Pile) pileView.getModelElement();
            Pile sourcePile = (Pile) fromWidget.getModelElement();

            CardView cardView = (CardView) draggingWidget;
            Card draggedCard = (Card) cardView.getModelElement();
            Move move = new PileToAceFoundationMove(sourcePile, draggedCard, targetPile,
                    (MultiDeck) deck.getModelElement());

            if (move.doMove(theGame)) {
                theGame.pushMove(move);
                theGame.refreshWidgets();
            } else {
                fromWidget.returnWidget(draggingWidget);
            }
        }

        c.releaseDraggingObject();
        c.repaint();
    }
}
