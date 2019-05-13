package com.ericchee.dragndrop

import android.content.ClipData
import android.content.ClipDescription
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val clipDescription = ClipDescription.MIMETYPE_TEXT_PLAIN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnDragMe.tag = "hello"

        btnDragMe.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                startDrag(v)
                return@setOnTouchListener false
            }
            true
        }

        allowDrag(bottomLeftContainer)
        allowDrag(bottomRightContainer)
        allowDrag(topContainer)
    }

    private fun startDrag(view: View) {
        val clipDataItem = ClipData.Item(view.tag as CharSequence)

        val mimeTypes = arrayOf(clipDescription)

        val clipData = ClipData(view.tag.toString(), mimeTypes, clipDataItem)

        val shadowBuilder = View.DragShadowBuilder(view)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            view.startDragAndDrop(clipData, shadowBuilder, view, 0)
        } else {
            view.startDrag(clipData, shadowBuilder, view, 0)
        }

        view.visibility = View.INVISIBLE
    }

    private fun allowDrag(dragDestinationView: View) {
        val originalBackground = dragDestinationView.background
        dragDestinationView.setOnDragListener { view, event ->
            val action = event.action

            when(event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    if (event.clipDescription.hasMimeType(clipDescription)) {

                        // show view can be accepted
//                        view.setBackgroundResource(R.color.yellow)
                        return@setOnDragListener true
                    }

                    // Returns false. During the current drag and drop operation, this View will
                    // not receive events again until ACTION_DRAG_ENDED is sent.
                    return@setOnDragListener false
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    view.setBackgroundResource(R.color.yellow)
                    view.invalidate() // redraw itself
                    return@setOnDragListener true
                }
                DragEvent.ACTION_DRAG_LOCATION -> return@setOnDragListener true
                DragEvent.ACTION_DRAG_EXITED -> {
                    // undo whatever you did in Drag Started & entered
                    view.background = originalBackground
                    view.invalidate()
                    return@setOnDragListener true
                }
                DragEvent.ACTION_DROP -> {
                    val clipDataItem = event.clipData.getItemAt(0)
                    val dragData = clipDataItem.text.toString()

                    Toast.makeText(this, "Dragged data is $dragData", Toast.LENGTH_SHORT).show()

                    view.background = originalBackground
                    view.invalidate()

                    val draggabbleView = event.localState as View
                    with(draggabbleView) {
                        val owner = parent as ViewGroup
                        if (owner != dragDestinationView) {
                            owner.removeView(this)
                            val container = view as FrameLayout
                            container.addView(draggabbleView)
                            draggabbleView.visibility = View.VISIBLE
                            return@setOnDragListener true
                        }
                    }
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    view.background = originalBackground
                    view.invalidate()

                    if (event.result) {
                        // drag worked
                    } else {
                        // drag didnt work
                        val draggabbleView = event.localState as View
                        draggabbleView.visibility = View.VISIBLE

                    }
                }
            }

            false
        }
    }
}
