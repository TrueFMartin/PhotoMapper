package com.github.truefmartin.photomapper.NewEditTaskActivity

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.github.truefmartin.photomapper.R

class RecurringSpinner(
    ctx: Context,
    spinner: Spinner,
    startingSelection: RecurringState,
    private val onSelectFunction: (String) -> Unit
):  AdapterView.OnItemSelectedListener{

    init {

        ArrayAdapter.createFromResource(
            ctx,
            R.array.recurring_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears.
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner.
            spinner.adapter = adapter
            spinner.setSelection(adapter.getPosition(startingSelection.displayStr))
        }
        spinner.onItemSelectedListener = this
    }


    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        Log.d("RecurringSpinner", "Selected at position $pos")
        // An item is selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos).
        onSelectFunction(parent.getItemAtPosition(pos).toString())
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Another interface callback.
        Log.d("RecurringSpinner", "Nothing selected")

    }
}