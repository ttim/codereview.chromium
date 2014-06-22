package com.chrome.codereview.tablet;

import android.content.Context;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.chrome.codereview.DiffAdapter;
import com.chrome.codereview.R;
import com.chrome.codereview.model.Comment;
import com.chrome.codereview.model.FileDiff;
import com.chrome.codereview.utils.ViewUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by sergeyv on 21/6/14.
 */
public class SideBySideDiffAdapter extends DiffAdapter {

    private static final int TYPE_SKIP = 0;
    private static final int TYPE_DIFF = 1;
    private static final int TYPE_COMMENTS = 2;
    private static final int NO_LINE_NUMBER = -1;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    private static class CommentPair {
        Comment left;
        Comment right;
    }

    private static class SkippingLine {

    }

    private static class Content {
        private int leftLineNumber = NO_LINE_NUMBER;
        private int rightLineNumber = NO_LINE_NUMBER;
        private boolean isChanged = true;
        private String left = "";
        private String right = "";
    }

    private final List<Object> mergedDiffLines;
    private List<Object> linesWithComments = new ArrayList<Object>();
    private final LayoutInflater inflater;

    public SideBySideDiffAdapter(Context context, FileDiff diff, List<Comment> comments) {
        super(context, diff, comments);
        inflater = LayoutInflater.from(context);
        mergedDiffLines = new ArrayList<Object>(diff.content().size());
        LinkedList<Content> left = new LinkedList<Content>();
        for (FileDiff.DiffLine diffLine : diff.content()) {
            switch (diffLine.type()) {
                case MARKER:
                    mergedDiffLines.addAll(left);
                    left.clear();
                    mergedDiffLines.add(new SkippingLine());
                    break;
                case BOTH_SIDE:
                    mergedDiffLines.addAll(left);
                    left.clear();
                    Content content = new Content();
                    content.isChanged = false;
                    content.leftLineNumber = diffLine.leftLineNumber();
                    content.rightLineNumber = diffLine.rightLineNumber();
                    content.left = diffLine.text().substring(1);
                    content.right = diffLine.text().substring(1);
                    mergedDiffLines.add(content);
                    break;
                case LEFT:
                    Content leftContent = new Content();
                    leftContent.leftLineNumber = diffLine.leftLineNumber();
                    leftContent.left = diffLine.text().substring(1);
                    left.add(leftContent);
                    break;
                case RIGHT:
                    Content rightContent = left.isEmpty() ? new Content() : left.removeFirst();
                    rightContent.right = diffLine.text().substring(1);
                    rightContent.rightLineNumber = diffLine.rightLineNumber();
                    mergedDiffLines.add(rightContent);
                    break;
            }
        }
        resetComments(comments);
    }

    @Override
    public int getCount() {
        return linesWithComments.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        Object object = linesWithComments.get(position);
        if (object instanceof SkippingLine) {
            return TYPE_SKIP;
        } else {
            return object instanceof CommentPair ? TYPE_COMMENTS : TYPE_DIFF;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        switch (getItemViewType(position)) {
            case TYPE_DIFF:
                return getDiffView((Content) linesWithComments.get(position), convertView, parent);
            case TYPE_COMMENTS:
                return getCommentsView((CommentPair) linesWithComments.get(position), convertView, parent);
            case TYPE_SKIP:
                return getSkipLinesView(convertView, parent);
        }
        throw new IllegalStateException("Unreachable");
    }

    private View getCommentsView(CommentPair commentPair, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.double_comment_item, parent, false);
        }
        View leftCommentView = convertView.findViewById(R.id.left_comment);
        View rightCommentView = convertView.findViewById(R.id.right_comment);
        leftCommentView.setVisibility(commentPair.left != null ? View.VISIBLE : View.INVISIBLE);
        rightCommentView.setVisibility(commentPair.right != null ? View.VISIBLE : View.INVISIBLE);
        if (commentPair.left != null) {
            fillCommentView(commentPair.left, leftCommentView);
        }
        if (commentPair.right != null) {
            fillCommentView(commentPair.right, rightCommentView);
        }
        return convertView;
    }

    private View getSkipLinesView(View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            ViewUtils.setText(convertView, android.R.id.text1, "skipping lines");
        }
        return convertView;
    }

    private void initDiffLines(View convertView, int id, String line, int color, int lineNumber) {
        View lineView = convertView.findViewById(id);
        lineView.setBackgroundColor(context.getResources().getColor(color));
        ViewUtils.setText(convertView, id, line);
        lineView.setTag(lineNumber);
        if (!TextUtils.isEmpty(line)) {
            lineView.setOnClickListener(this);
        }
    }

    private View getDiffView(Content content, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.side_by_side_diff_item, parent, false);
        }
        int leftColor = content.isChanged ? R.color.diff_remove : R.color.diff_default_background;
        int rightColor = content.isChanged ? R.color.diff_add : R.color.diff_default_background;

        initDiffLines(convertView, R.id.left, content.left, leftColor, content.leftLineNumber);
        initDiffLines(convertView, R.id.right, content.right, rightColor, content.rightLineNumber);
        return convertView;
    }

    @Override
    protected void rebuildWithComments(HashMap<Pair<Integer, Boolean>, List<Comment>> lineToComments) {

        linesWithComments.clear();
        for (Object object : mergedDiffLines) {
            linesWithComments.add(object);
            if (object instanceof SkippingLine) {
                continue;
            }
            Content content = (Content) object;
            List<Comment> leftComments = lineToComments.get(new Pair<Integer, Boolean>(content.leftLineNumber, true));
            List<Comment> rightComments = lineToComments.get(new Pair<Integer, Boolean>(content.rightLineNumber, false));
            Iterator<Comment> leftIterator = leftComments != null ? leftComments.iterator() : Collections.<Comment>emptyListIterator();
            Iterator<Comment> rightIterator = rightComments != null ? rightComments.iterator() : Collections.<Comment>emptyListIterator();

            while (leftIterator.hasNext() || rightIterator.hasNext()) {
                Comment leftComment = leftIterator.hasNext() ? leftIterator.next() : null;
                Comment rightComment = rightIterator.hasNext() ? rightIterator.next() : null;
                CommentPair commentPair = new CommentPair();
                commentPair.left = leftComment;
                commentPair.right = rightComment;
                linesWithComments.add(commentPair);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.left || v.getId() == R.id.right) {
            int lineNumber = (Integer) v.getTag();
            commentActionListener.writeComment(lineNumber, v.getId() == R.id.left);
            return;
        }
        super.onClick(v);
    }
}
