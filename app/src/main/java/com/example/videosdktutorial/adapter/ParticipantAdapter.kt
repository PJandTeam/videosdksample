package com.example.videosdktutorial.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.videosdktutorial.R
import live.videosdk.rtc.android.Meeting
import live.videosdk.rtc.android.Participant
import live.videosdk.rtc.android.Stream
import live.videosdk.rtc.android.VideoView
import live.videosdk.rtc.android.listeners.MeetingEventListener
import live.videosdk.rtc.android.listeners.ParticipantEventListener
import org.webrtc.VideoTrack

class ParticipantAdapter(meeting: Meeting) :
    RecyclerView.Adapter<ParticipantAdapter.PeerViewHolder>() {

    private var containerHeight = 0
    private val participants: MutableList<Participant> = ArrayList()

    init {
        // adding the local participant(You) to the list
        participants.add(meeting.localParticipant)

        // adding Meeting Event listener to get the participant join/leave event in the meeting.
        meeting.addEventListener(object : MeetingEventListener() {
            override fun onParticipantJoined(participant: Participant) {
                // add participant to the list
                participants.add(participant)
                notifyItemInserted(participants.size - 1)
            }

            override fun onParticipantLeft(participant: Participant) {
                var pos = -1
                for (i in participants.indices) {
                    if (participants[i].id == participant.id) {
                        pos = i
                        break
                    }
                }
                // remove participant from the list
                participants.remove(participant)
                if (pos >= 0) {
                    notifyItemRemoved(pos)
                }
            }
        })
    }

    // replace getItemCount() method with following.
    // this method returns the size of total number of participants
    override fun getItemCount(): Int {
        return participants.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeerViewHolder {
        containerHeight = parent.height
        return PeerViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_remote_peer, parent, false)
        )
    }

    override fun onBindViewHolder(holder: PeerViewHolder, position: Int) {
        val participant = participants[position]

        val layoutParams = holder.itemView.layoutParams
        layoutParams.height = containerHeight / 3
        holder.itemView.layoutParams = layoutParams

        holder.tvName.text = participant.displayName

        // adding the initial video stream for the participant into the 'VideoView'
        for ((_, stream) in participant.streams) {
            if (stream.kind.equals("video", ignoreCase = true)) {
                holder.participantView.visibility = View.VISIBLE
                val videoTrack = stream.track as VideoTrack
                holder.participantView.addTrack(videoTrack)
                break
            }
        }

        // add Listener to the participant which will update start or stop the video stream of that participant
        participant.addEventListener(object : ParticipantEventListener() {
            override fun onStreamEnabled(stream: Stream) {
                if (stream.kind.equals("video", ignoreCase = true)) {
                    holder.participantView.visibility = View.VISIBLE
                    holder.tvParticipantName.visibility = View.GONE
                    val videoTrack = stream.track as VideoTrack
                    holder.participantView.addTrack(videoTrack)
                }
            }

            override fun onStreamDisabled(stream: Stream) {
                //To get first letter of first name and last name
                val fullName = participant.displayName
                val parts = fullName.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val firstName = parts[0]
                val lastName = parts[1]
                val firstInitial = firstName[0]
                val lastInitial = lastName[0]

                if (stream.kind.equals("video", ignoreCase = true)) {
                    holder.participantView.removeTrack()
                    holder.participantView.visibility = View.GONE
                    holder.tvParticipantName.visibility = View.VISIBLE
                    holder.tvParticipantName.text = firstInitial + lastInitial.toString()
                }
            }
        })
    }

    class PeerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var participantView: VideoView = view.findViewById(R.id.participantView)
        var tvName: TextView = view.findViewById(R.id.tvName)
        var tvParticipantName: TextView = view.findViewById(R.id.tvParticipantName)
    }
}