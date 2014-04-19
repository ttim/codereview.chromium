package com.chrome.codereview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.chrome.codereview.model.Issue;
import com.chrome.codereview.model.UserIssues;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergeyv on 20/4/14.
 */
class UserIssuesAdapter extends BaseAdapter {

    private static final int TYPE_ISSUE = 0;
    private static final int TYPE_GROUP_HEADER = 1;

    private static class Box {

        Issue issue;
        int titleResource;

        private Box(int titleResource) {
            this.titleResource = titleResource;
        }

        private Box(Issue issue) {
            this.issue = issue;
        }

        boolean isBoxIssue() {
            return issue != null;
        }

    }

    private List<Box> boxes = new ArrayList<Box>();

    private final LayoutInflater inflater;

    public UserIssuesAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return boxes.size();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public Issue getItem(int position) {
        Box box = boxes.get(position);
        return box.isBoxIssue() ? box.issue : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setUserIssues(UserIssues userIssues) {
        boxes.clear();
        if (userIssues == null) {
            notifyDataSetChanged();
            return;
        }
        addGroup(R.string.header_incoming_reviews, userIssues.incomingReviews());
        addGroup(R.string.header_outgoing_reviews, userIssues.outgoingReviews());
        addGroup(R.string.header_cced_reviews, userIssues.ccReviews());
        addGroup(R.string.header_recently_closed, userIssues.recentlyClosed());
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return boxes.get(position).isBoxIssue() ? TYPE_ISSUE : TYPE_GROUP_HEADER;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (getItemViewType(position) == TYPE_GROUP_HEADER) {
            return getGroupHeaderView(boxes.get(position).titleResource, convertView, parent);
        }

        return getIssueView(boxes.get(position).issue, convertView, parent);
    }

    private View getIssueView(Issue issue, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.issue_item, parent, false);
        }
        TextView subjectTextView = (TextView) convertView.findViewById(R.id.subject);
        TextView ownerTextView = (TextView) convertView.findViewById(R.id.owner);
        subjectTextView.setText(issue.subject());
        ownerTextView.setText(issue.owner());

        return convertView;
    }

    private View getGroupHeaderView(int titleRes, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_group_header, parent, false);
        }
        TextView titleView = (TextView) convertView;
        titleView.setText(titleRes);
        return convertView;
    }

    private void addGroup(int titleRes, List<Issue> issues) {
        if (issues.isEmpty()) {
            return;
        }
        boxes.add(new Box(titleRes));
        for (Issue issue: issues) {
            boxes.add(new Box(issue));
        }
    }
}
