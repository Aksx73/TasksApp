package com.absut.tasksapp.ui.tasks

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.absut.tasksapp.R
import com.absut.tasksapp.databinding.FragmentTasksBinding
import com.absut.tasksapp.util.Constants
import com.absut.tasksapp.util.Util.showSnackbarWithAnchor
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.fragment_tasks) {

    private val tabTitleArray = arrayOf(
        "TODO",
        "Completed"
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentTasksBinding.bind(view)

        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout

        val adapter = ViewPagerAdapter(childFragmentManager, lifecycle)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitleArray[position]
        }.attach()

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(
                TasksFragmentDirections.actionTaskFragmentToAddEditTaskFragment(
                    null
                )
            )
        }

        setFragmentResultListener("add_edit_request") { _, bundle ->
            val result = bundle.getInt("add_edit_result")
            Log.d("TAG", "onViewCreated: $result")
            when(result){
                Constants.ADD_TASK_RESULT_OK -> {
                    binding.fabAdd.showSnackbarWithAnchor("Task added")
                }
                Constants.EDIT_TASK_RESULT_OK -> {
                    binding.fabAdd.showSnackbarWithAnchor("Task updated")
                }
            }
        }
    }

    inner class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
        FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int {
            return NUM_TABS
        }

        override fun createFragment(position: Int): Fragment {
            when (position) {
                0 -> return TodoTaskFragment()
                1 -> return CompletedTaskFragment()
            }
            return TodoTaskFragment()
        }
    }


    companion object {
        private const val NUM_TABS = 2
    }
}