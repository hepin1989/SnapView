package us.sosia.snapview;

import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.Scroller;

public class SnapView extends FrameLayout {
	//TODO lock the event dispatch direction
	private TopView mTopView;
	private FrameLayout mLeftView;
	private FrameLayout mRightView;
	protected GestureDetector mGestureDetector;
	
	private RectF mLeftBoundRect;
	private RectF mRightBoundRect;
	
	private Distnation pDistnation = Distnation.TOP;
	private boolean pIsLeftViewEnable = false;
	private boolean pIsRightViewEnable = false;
	private boolean pDispatchToTop = true;  
	private boolean pLockDispatch = true;

	private enum Position{
		LEFT,CENTER,RIGHT
	}
	
	private enum Direction{
		LEFT,RIGHT,IDLE
	}
	
	private enum State{
		SCROLL,FLING,IDLE
	}
	
/*	private enum Mode{
		STEP,SIDE,BLINK
	}*/
	
	private enum Distnation{
		TOP,LEFT,RIGHT,OUTTER
	}
	
	/**
	 * set the left view of the snap view ,when the left view is set, you can snap your top view to right and center.
	 * @param leftView The left side view to display
	 * **/
	public void setLeftView(View leftView) {
 
		
		if (mLeftView.getChildCount() != 0) {
			mLeftView.removeAllViews();
		}
		mLeftView.addView(leftView);
		pIsLeftViewEnable = true;
	}
	/**
	 * set the right view of the snap view ,when the right view is set, you can snap your top view to left and center.
	 * @param rightView The right side view to display
	 * **/
	public void setRightView(View rightView) {
 
		if (mRightView.getChildCount() != 0) {
			mRightView.removeAllViews();
		}
		mRightView.addView(rightView);
		pIsRightViewEnable = true;
	}
	
	/**
	 * set the top view of the snap view.the top view must be set,and the top view is which view you can snap to left
	 * and right.the enabled snap direction is depends on whether the left_view or right_view is set.
	 * @param topView The top view of the snap view.
	 * */
	public void setTopView(View topView) {
		if (mTopView.getChildCount() != 0) {
			mTopView.removeAllViews();
		}
		mTopView.addView(topView);
	}
	

	public void setLeftViewBackGroundCorlor(int corlor){
		mLeftView.setBackgroundColor(corlor);
	}
	
	public void setRightViewBackGroundCorlor(int corlor){
		mRightView.setBackgroundColor(corlor);
	}
	
	public RectF getmLeftBoundRect() {
		return mLeftBoundRect;
	}

	public RectF getmRightBoundRect() {
		return mRightBoundRect;
	}

	public SnapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initSnapView();
 	}

	public SnapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initSnapView();
 	}

	public SnapView(Context context) {
		super(context);
		initSnapView();
 	}
		
	private void  initSnapView(){
		mGestureDetector = new GestureDetector(getContext(),  new GestureListener());
		mLeftView = new FrameLayout(getContext());
		mRightView = new FrameLayout(getContext());
		mTopView = new TopView(getContext());
		addView(mLeftView);
		addView(mRightView);
		addView(mTopView);
	}
	/**
	 * Set the interpolator that snapping animation use
	 * @param interpolator The interpolator used by the top view to snap left and right 
	 * **/
	public void SetInterpolator(Interpolator interpolator){
 		mTopView.setInterpolator(interpolator);
	}
	
	
	/**
	 * How quick should the snapping should be
	 * @param duration The duration to snap left and right
	 * **/
	public void setDuration(int duration){
		mTopView.setScrollDuration(duration);
	}
	
	private boolean isPointInRect(float x,float y,Position currentPosition){
		switch (currentPosition) {
		case CENTER:
			return false;
 		case LEFT:
 			return mLeftBoundRect.contains(x, y);
		case RIGHT:
			return mRightBoundRect.contains(x,y);
		default:
			break;
		}
		return false;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		//bound,scroll DIrection
 		mGestureDetector.onTouchEvent(ev);

			if (pLockDispatch) {
				switch (pDistnation) {
					case TOP:
						//Log.i("gg", "top");
						return mTopView.dispatchTouchEvent(ev);
		 			case LEFT:
						//Log.i("gg", "left");
						return mLeftView.dispatchTouchEvent(ev);
 		 			case RIGHT:
						//Log.i("gg", "right");
						return mRightView.dispatchTouchEvent(ev);
 		 			default:
						break;
				}
			}
		if (mTopView.getState() == State.SCROLL && ev.getAction() == MotionEvent.ACTION_UP ) {
			mTopView.reset();
			//mTopView. = State.IDLE;
			mTopView.setState(State.IDLE);
		}
	
 		return true;
	}


	private void resetState() {
		pLockDispatch = false;
		pDispatchToTop = true;
	}

	private class GestureListener implements OnGestureListener{

		@Override
		public boolean onDown(MotionEvent e) {
			resetState();
			//here must dispatch the event to the responsible view
			switch (mTopView.getPosition()) {
			case CENTER:
				return mTopView.dispatchTouchEvent(e);
 			case LEFT:
 				if (!isPointInRect(e.getX(), e.getY(), Position.LEFT)) {
 					return	mRightView.dispatchTouchEvent(e);
				}
				break;
			case RIGHT:
				if (!isPointInRect(e.getX(), e.getY(), Position.RIGHT)) {
					return mLeftView.dispatchTouchEvent(e);
				}
				break;
			default:
				break;
			}
 			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {
 			
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
		//	resetState();
			Position currentPosition = mTopView.getPosition();
			switch (currentPosition) {
				case LEFT:
					if (isPointInRect(e.getX(), e.getY(), Position.LEFT)) {
						mTopView.snapToCenter();
						return true;
					}else {
						//stup
						//to right
						return mRightView.dispatchTouchEvent(e);
					}
 				case RIGHT:
					if (isPointInRect(e.getX(), e.getY(), Position.RIGHT)) {
						mTopView.snapToCenter();
						return true;
					}else {
						//stup
						//to left
						return mLeftView.dispatchTouchEvent(e);                      
					}
 	
				default:
					break;
			}
			
			return false;
		}


		
		
		
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			
			//sroll <--------->
			if (!pLockDispatch) {
				//lock the dispatch direction in the first scroll event
				pLockDispatch = true;
				
				//dispatch an cancle to all view
				MotionEvent motionEvent = MotionEvent.obtain(e2);
				motionEvent.setAction(MotionEvent.ACTION_CANCEL);
				
 				
				if (Math.abs(distanceX) > Math.abs(distanceY)) {
 					pDispatchToTop = true;
					pDistnation = Distnation.OUTTER;
					
					mTopView.dispatchTouchEvent(motionEvent);
					mLeftView.dispatchTouchEvent(motionEvent);
					mRightView.dispatchTouchEvent(motionEvent);

				}else {
					//scroll  ^	
 					Position tempPosition = mTopView.getPosition();
 					switch (tempPosition) {
					case CENTER:
	 					pDispatchToTop = true;
						pDistnation = Distnation.TOP;
						break;
					case LEFT:
						if (!isPointInRect(e1.getX(), e1.getY(), tempPosition)) {
							pDistnation = Distnation.RIGHT;
		 					pDispatchToTop = false;
						}
						break;
					case RIGHT:
						if (!isPointInRect(e1.getX(), e1.getY(), tempPosition)) {
							pDistnation = Distnation.LEFT;
		 					pDispatchToTop = false;
						}
						break;
					default:
						break;
					}
 					
 					
				}
			}else if (pDispatchToTop && pDistnation == Distnation.OUTTER) {
				
				mTopView.scroll((int)distanceX, 25);
			}

 			return true;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			MotionEvent motionEvent = MotionEvent.obtain(e);
			motionEvent.setAction(MotionEvent.ACTION_CANCEL);
			
			mTopView.dispatchTouchEvent(motionEvent);
			mLeftView.dispatchTouchEvent(motionEvent);
			mRightView.dispatchTouchEvent(motionEvent);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			resetState();
			if (velocityX > 0) {
				snapToNext(Direction.RIGHT);
			}else {
				snapToNext(Direction.LEFT);
			}
 			return true;
		}
		
	}
	


	private void snapToNext(Direction snapDirection){
		Position currentPosition = mTopView.getPosition();
		switch (snapDirection) {
		case LEFT:
		{
			switch (currentPosition) {
				case RIGHT:
					mTopView.snapToCenter();
					break;
				case CENTER:
					if (pIsRightViewEnable) {
						mTopView.snapToLeft();
					}
					break;
				default:
					break;
			}
		}
			break;
		case RIGHT:
		{
				switch (currentPosition) {
				case LEFT:
					mTopView.snapToCenter();
					break;
				case CENTER:
					if (pIsLeftViewEnable) {
						mTopView.snapToRight();
					}
					break;
				default:
					break;
			}
		}
			break;
		case IDLE:
			break;
		default:
			break;
		}
	}




/***
 * Top view is a view witch can snap left or right in the snap view group.to use it ,just add your view as an child to
 * it,ever more,you can add your own view group to it too!
 * Top view is always been used with the snap view,and you just interactive with it with the setTopView function.
 * 
 * ***/
	public class TopView extends FrameLayout{
		private int pLeftBound = 10;
		private int pRightBound = 10;
		private int pScrollToLeft = 0;
		private int pScrollToRight = 0;
		private int pCenterPosition = 0;
		private int pScrollDuration = 600;
		
		private Position pPosition = Position.CENTER;
		private State pState = State.IDLE;
		private Direction pDirection = Direction.IDLE;
		
		
 		private Scroller mScroller ;
 		private TopViewListener mTopViewListener;
 		private Interpolator mInterpolator = AnimationUtils.loadInterpolator(getContext(), android.R.anim.accelerate_decelerate_interpolator);

		public TopView(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
			initTopView();
 		}

		public TopView(Context context, AttributeSet attrs) {
			super(context, attrs);
			initTopView();
 		}

		public TopView(Context context) {
			super(context);
			initTopView();
 		}

		private void initTopView(){
			mScroller = new Scroller(getContext(),mInterpolator);
 		}
		
		public void setInterpolator(Interpolator interpolator){
			if (interpolator != null) {
				mInterpolator = interpolator;
				mScroller = new Scroller(getContext(), mInterpolator);
			}
		}
		
		
		
		
		
/*		@Override
		public boolean dispatchTouchEvent(MotionEvent ev) {
			
			return true;	
		}*/

		public TopViewListener getTopViewListener() {
			return mTopViewListener;
		}

		public void setmTopViewListener(TopViewListener topViewListener) {
			this.mTopViewListener = topViewListener;
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
 			super.onSizeChanged(w, h, oldw, oldh);
 			pCenterPosition = w/2;
 			pScrollToLeft = - (w - w / pLeftBound);
 			pScrollToRight = w - w / pRightBound;
 			mRightBoundRect = new RectF(pScrollToRight, 0, w, h);
 			mLeftBoundRect = new RectF(0, 0, w/pLeftBound, h);
		}
		


		@Override
		public void computeScroll() {
			if (mScroller.computeScrollOffset()) {
				scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
				postInvalidate();
			}else {
				//reset();
				//pState = State.IDLE;
			}
 		}

		/**
		 * Reset the top view to the corresponding position
		 * */
		public void reset(){
			if (isNeedReset()) {
				resetTo(resetToWhere());
			}
		}
		
		public boolean isNeedReset(){
			int cx = mScroller.getCurrX(); 
			return cx != 0 && cx != pLeftBound && cx != pRightBound;
		}
		
 		public Position resetToWhere(){
			if (Math.abs(mScroller.getCurrX()) > pCenterPosition) {
				if (mScroller.getCurrX()>0) {
					return Position.LEFT;
				}else {
					return Position.RIGHT;
				}
			}else {
				return Position.CENTER;
			}
		}
		
 		public void resetTo(Position resetToPosition){
 			switch (resetToPosition) {
			case LEFT:
				resetToLeft();
				break;
			case CENTER:
				resetToCenter();
				break;
			case RIGHT:
				resetToRight();
				break;
			default:
				break;
			}
 		}
		
		
		public void resetToLeft(){
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			mScroller.startScroll(mScroller.getCurrX(), 0, 0,0,pScrollDuration/2);
			mScroller.setFinalX(pScrollToRight);
			invalidate();
			
			pPosition = Position.LEFT;
			pState = State.FLING;
		}
		
		public void resetToRight(){
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			mScroller.startScroll(mScroller.getCurrX(), 0, 0,0,pScrollDuration/2);
			mScroller.setFinalX(pScrollToLeft);
			invalidate();
			
			pPosition = Position.RIGHT;
			pState = State.FLING;
		}
		
		public void resetToCenter(){
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			mScroller.startScroll(mScroller.getCurrX(), 0, 0,0,pScrollDuration/2);
			mScroller.setFinalX(0);
			invalidate();
			
			pState = State.FLING;
			pPosition = Position.CENTER;
		}
		
		
		public void snapToLeft(){
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			mScroller.startScroll(mScroller.getCurrX(), 0, 0,0,pScrollDuration);
			mScroller.setFinalX(pScrollToRight);
			invalidate();
			if (mTopViewListener != null) {
				mTopViewListener.snapToLeft();
			}
			pPosition = Position.LEFT;
			pState = State.FLING;
 		}
		public void snapToCenter(){
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			mScroller.startScroll(mScroller.getCurrX(), 0, 0,0,pScrollDuration);
			mScroller.setFinalX(0);
			invalidate();
			if (mTopViewListener != null) {
				mTopViewListener.snapToCenter();
			}
			pState = State.FLING;
			pPosition = Position.CENTER;
 		}
		public void snapToRight(){
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			mScroller.startScroll(mScroller.getCurrX(), 0, 0,0,pScrollDuration);
			mScroller.setFinalX(pScrollToLeft);
			invalidate();
			if (mTopViewListener != null) {
				mTopViewListener.snapToRight();
			}
			pPosition = Position.RIGHT;
			pState = State.FLING;
		}
		
		/**
		 * @deprecated
		 * */
		public boolean canScrollMore(int afterPosition){
			if (pIsLeftViewEnable) {
				return afterPosition > pScrollToLeft && afterPosition < 0;
			}
			else
			if (pIsRightViewEnable) {
				return afterPosition > 0 && afterPosition < pScrollToRight;
			}
			else
			if (pIsLeftViewEnable && pIsRightViewEnable) {
				return afterPosition > pScrollToLeft && afterPosition < pScrollToRight;
			}
			return false;
		}
		
		public void scroll(int distansX,int duration){
			pDirection = distansX > 0 ? Direction.RIGHT : Direction.LEFT;
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			
			int currentX = mScroller.getCurrX();
			
			
			//aboard the scroll in some situation
			
			if (currentX >= 0 && pIsLeftViewEnable && !pIsRightViewEnable) {
				if (pDirection == Direction.RIGHT) {
					return;
				}
			}else
			if (currentX <= 0 && pIsRightViewEnable && !pIsLeftViewEnable) {
				if (pDirection == Direction.LEFT) {
					return;
				}
			}
			
			mScroller.startScroll(currentX, 0, distansX, 0,duration);
			
			if (currentX + distansX < pScrollToLeft) {
				mScroller.setFinalX(pScrollToLeft);
			}else if (currentX + distansX > pScrollToRight) {
				mScroller.setFinalX(pScrollToRight);
			}
			invalidate();
			
			pState = State.SCROLL;
/*			pDirection = distansX > 0 ? Direction.LEFT:Direction.RIGHT;
*/		}
		
		
		public int getScrollDuration() {
			return pScrollDuration;
		}

		public void setScrollDuration(int pScrollDuration) {
			this.pScrollDuration = pScrollDuration;
		}

		public Position getPosition() {
			return pPosition;
		}

		public int getLeftBound() {
			return pLeftBound;
		}

		public void setLeftBound(int pLeftBound) {
			this.pLeftBound = pLeftBound;
		}

		public int getRightBound() {
			return pRightBound;
		}

		public void setRightBound(int pRightBound) {
			this.pRightBound = pRightBound;
		}

		public int getScrollToLeft() {
			return pScrollToLeft;
		}

		public void setScrollToLeft(int pScrollToLeft) {
			this.pScrollToLeft = pScrollToLeft;
		}

		public int getScrollToRight() {
			return pScrollToRight;
		}

		public void setScrollToRight(int pScrollToRight) {
			this.pScrollToRight = pScrollToRight;
		}

		public int getCenterPosition() {
			return pCenterPosition;
		}

		public void setCenterPosition(int pCenterPosition) {
			this.pCenterPosition = pCenterPosition;
		}

		public State getState() {
			return pState;
		}
		
		

		public void setState(State pState) {
			this.pState = pState;
		}

		public Direction getDirection() {
			return pDirection;
		}

		
	}
	/**
	 * This listener's function is fired once the snap view's top view is 
	 * snap to left,right or center.
	 * ***/
	public static abstract interface TopViewListener{
		/**
		 * Called when the top view is snap to left.
		 * */
		public void snapToLeft();
		/**
		 * Called when the top view is snap to right.
		 * */
		public void snapToRight();
		/**
		 * Called when the top view is snap to center.
		 * */
		public void snapToCenter();
	}
	
	
	
	
	
	
}
