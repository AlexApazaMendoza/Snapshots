package com.example.snapshots

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.snapshots.databinding.FragmentProfileBinding
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var mBinding: FragmentProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = FragmentProfileBinding.inflate(inflater,container, false)
        return  mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.tvName.text = FirebaseAuth.getInstance().currentUser?.displayName
        mBinding.tvEmail.text = FirebaseAuth.getInstance().currentUser?.email
        mBinding.btnLogout.setOnClickListener {
            signOut()
        }
    }

    private fun signOut() {
        context?.let {
            AuthUI
                .getInstance()
                .signOut(it)
                .addOnCompleteListener{
                    Toast.makeText(context,"Hasta pronto...",Toast.LENGTH_SHORT).show()
                }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}