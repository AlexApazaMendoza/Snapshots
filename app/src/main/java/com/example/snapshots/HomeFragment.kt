package com.example.snapshots

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.snapshots.databinding.FragmentHomeBinding
import com.example.snapshots.databinding.ItemSnapshotBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment(), HomeAux {

    private lateinit var mBinding: FragmentHomeBinding

    private lateinit var mFirebaseAdapter:FirebaseRecyclerAdapter<Snapshot, SnapshotHolder>

    private lateinit var mLayoutManager : RecyclerView.LayoutManager

    private var param1: String? = null
    private var param2: String? = null

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
        mBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return mBinding.root
        // Inflate the layout for this fragment
        /*return inflater.inflate(R.layout.fragment_home, container, false)*/
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /***
         * Adapter with Firebase
         */
        val query = FirebaseDatabase.getInstance().reference.child("snapshots")
        /*val options = FirebaseRecyclerOptions
            .Builder<Snapshot>()
            .setQuery(query,Snapshot::class.java)
            .build()*/

        val options = FirebaseRecyclerOptions
            .Builder<Snapshot>()
            .setQuery(query) {
                val snapshot = it.getValue(Snapshot::class.java)
                snapshot!!.id = it.key
                snapshot
            }.build()

        mFirebaseAdapter = object :FirebaseRecyclerAdapter<Snapshot, SnapshotHolder>(options){
            private lateinit var mContext:Context

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SnapshotHolder {
                mContext = parent.context
                val viewF = LayoutInflater.from(mContext).inflate(R.layout.item_snapshot, parent,false)
                return SnapshotHolder(viewF)
            }

            override fun onBindViewHolder(holder: SnapshotHolder, position: Int, model: Snapshot) {
                val snapshot = getItem(position)

                with(holder){
                    setListener(snapshot)
                    binding.tvTitle.text = snapshot.title
                    binding.cbLike.text = (snapshot.likeList?.keys?.size ?: 0).toString()
                    binding.cbLike.isChecked = snapshot.likeList?.contains(FirebaseAuth.getInstance().uid) ?: false
                    Glide.with(mContext)
                        .load(snapshot.photoUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(binding.imgPhoto)

                    binding.btnDelete.visibility = if(snapshot.ownerUid == FirebaseAuth.getInstance().uid) View.VISIBLE else View.GONE
                }
            }

            @SuppressLint("NotifyDataSetChanged")   //Error interno FirebaseUI 8.0.0
            override fun onDataChanged() {
                super.onDataChanged()
                mBinding.progressBar.visibility = View.GONE
                notifyDataSetChanged()
            }

            override fun onError(error: DatabaseError) {
                super.onError(error)
                Toast.makeText(mContext,error.message,Toast.LENGTH_SHORT).show()
            }
        }

        mLayoutManager = LinearLayoutManager(context)

        mBinding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = mLayoutManager
            adapter = mFirebaseAdapter
        }
    }

    override fun onStart() {
        super.onStart()
        mFirebaseAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        mFirebaseAdapter.stopListening()
    }

    override fun goToTop() {
        mBinding.recyclerView.smoothScrollToPosition(0)
    }

    private fun deleteSnapshot(snapshot: Snapshot){
        val databaseReference =
            FirebaseDatabase
                .getInstance()
                .reference
                .child("snapshots")

        //databaseReference.child(snapshot.id!!).removeValue()

        val storageReference = FirebaseStorage.getInstance().reference
            .child("snapshots")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child(snapshot.id!!)

        storageReference.delete().addOnCompleteListener {
            if(it.isSuccessful){
                databaseReference.child(snapshot.id!!).removeValue()
            } else{
                Snackbar.make(mBinding.root,getString(R.string.home_delete_photo_error),Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun setLike(snapshot: Snapshot, checked: Boolean){
        val databaseReference= FirebaseDatabase.getInstance().reference.child("snapshots")
        if(checked){
            databaseReference.child(snapshot.id!!).child("likeList")
                .child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(checked)
        } else{
            databaseReference.child(snapshot.id!!).child("likeList")
                .child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(null)
        }
    }

    /***
     * Holder para FirebaseUI
     */
    inner class SnapshotHolder(view: View) : RecyclerView.ViewHolder(view){
        val binding = ItemSnapshotBinding.bind(view)

        fun setListener(snapshot: Snapshot){
            binding.btnDelete.setOnClickListener {
                deleteSnapshot(snapshot)
            }
            binding.cbLike.setOnCheckedChangeListener { _, checked ->
                setLike(snapshot, checked)
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
         * @return A new instance of fragment HomeFragment.
         */
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}