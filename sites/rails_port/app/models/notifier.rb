class Notifier < ActionMailer::Base
  def signup_confirm(user, token)
    recipients user.email
    from "webmaster@openstreetmap.org"
    subject "[OpenStreetMap] Confirm your email address"
    headers "Auto-Submitted" => "auto-generated"
    body :url => url_for(:host => SERVER_URL,
                         :controller => "user", :action => "confirm",
                         :confirm_string => token.token)
  end

  def lost_password(user, token)
    recipients user.email
    from "webmaster@openstreetmap.org"
    subject "[OpenStreetMap] Password reset request"
    headers "Auto-Submitted" => "auto-generated"
    body :url => url_for(:host => SERVER_URL,
                         :controller => "user", :action => "reset_password",
                         :email => user.email, :token => token.token)
  end

  def reset_password(user, pass)
    recipients user.email
    from "webmaster@openstreetmap.org"
    subject "[OpenStreetMap] Password reset"
    headers "Auto-Submitted" => "auto-generated"
    body :pass => pass
  end

  def gpx_success(trace, possible_points)
    recipients trace.user.email
    from "webmaster@openstreetmap.org"
    subject "[OpenStreetMap] GPX Import success"
    headers "Auto-Submitted" => "auto-generated"
    body :trace_name => trace.name,
         :trace_points => trace.size,
         :possible_points => possible_points
  end

  def gpx_failure(trace, error)
    recipients trace.user.email
    from "webmaster@openstreetmap.org"
    subject "[OpenStreetMap] GPX Import failure"
    headers "Auto-Submitted" => "auto-generated"
    body :trace_name => trace.name,
         :error => error
  end
  
  def message_notification(message)
    recipients message.recipient.email
    from "webmaster@openstreetmap.org"
    subject "[OpenStreetMap] #{message.sender.display_name} sent you a new message"
    headers "Auto-Submitted" => "auto-generated"
    body :to_user => message.recipient.display_name,
         :from_user => message.sender.display_name,
         :body => message.body,
         :subject => message.title,
         :readurl => url_for(:host => SERVER_URL,
                             :controller => "message", :action => "read",
                             :message_id => message.id),
         :replyurl => url_for(:host => SERVER_URL,
                              :controller => "message", :action => "reply",
                              :message_id => message.id)
  end

  def diary_comment_notification(comment)
    recipients comment.diary_entry.user.email
    from "webmaster@openstreetmap.org"
    subject "[OpenStreetMap] #{comment.user.display_name} commented on your diary entry"
    headers "Auto-Submitted" => "auto-generated"
    body :to_user => comment.diary_entry.user.display_name,
         :from_user => comment.user.display_name,
         :body => comment.body,
         :subject => comment.diary_entry.title,
         :readurl => url_for(:host => SERVER_URL,
                             :controller => "diary_entry",
                             :action => "view",
                             :display_name => comment.diary_entry.user.display_name,
                             :id => comment.diary_entry.id,
                             :anchor => "comment#{comment.id}"),
         :commenturl => url_for(:host => SERVER_URL,
                                :controller => "diary_entry",
                                :action => "view",
                                :display_name => comment.diary_entry.user.display_name,
                                :id => comment.diary_entry.id,
                                :anchor => "newcomment"),
         :replyurl => url_for(:host => SERVER_URL,
                              :controller => "message",
                              :action => "new",
                              :user_id => comment.user.id,
                              :title => "Re: #{comment.diary_entry.title}")
  end

  def friend_notification(friend)
    befriender = User.find_by_id(friend.user_id)
    befriendee = User.find_by_id(friend.friend_user_id)

    recipients befriendee.email
    from "webmaster@openstreetmap.org"
    subject "[OpenStreetMap] #{befriender.display_name} added you as a friend"
    headers "Auto-Submitted" => "auto-generated"
    body :user => befriender.display_name,
         :userurl => url_for(:host => SERVER_URL,
                             :controller => "user", :action => "view",
                             :display_name => befriender.display_name)
  end
end
