package com.absut.tasksapp.ui.tasks

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.absut.tasksapp.R
import com.absut.tasksapp.databinding.FragmentTasksBinding
import com.absut.tasksapp.util.Constants
import com.absut.tasksapp.util.Util.showSnackbarWithAnchor
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.fragment_tasks) {

    private val tabTitleArray = arrayOf(
        "Todo",
        "Completed"
    )

    private val tabIconArray = arrayOf(
        R.drawable.ic_task_pending_24,
        R.drawable.ic_task_completed_24
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
            tab.icon = ResourcesCompat.getDrawable(resources,tabIconArray[position],null)
        }.attach()

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(
                TasksFragmentDirections.actionTaskFragmentToAddEditTaskFragment(null)
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
                Constants.DELETE_TASK_RESULT_OK -> {
                    binding.fabAdd.showSnackbarWithAnchor("Task deleted")
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