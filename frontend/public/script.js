// Global state
let currentUser = null;
let currentChat = null;
let currentGroup = null;
let currentFriendId = null;
let currentFriendName = null;
const API_URL = 'http://localhost:8080/api';

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    console.log('✓ WhatsApp Frontend loaded');
});

// ==================== Auth Functions ====================

function switchTab(tabId) {
    // Hide all tabs
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    
    // Remove active class from buttons
    document.querySelectorAll('.tab-button').forEach(btn => {
        if (btn.onclick && btn.onclick.toString().includes(tabId)) {
            btn.classList.add('active');
        }
    });
    
    // Show selected tab
    document.getElementById(tabId).classList.add('active');
}

async function register() {
    const name = document.getElementById('registerName').value.trim();
    const phone = document.getElementById('registerPhone').value.trim();
    const statusDiv = document.getElementById('loginStatus');
    
    if (!name || !phone) {
        showStatus('Please fill all fields', 'error');
        return;
    }
    
    try {
        const response = await fetch(`${API_URL}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ profileName: name, phoneNumber: phone })
        });
        
        const data = await response.json();
        
        if (data.success) {
            showStatus('✓ Registration successful! Logging you in...', 'success');
            currentUser = {
                userId: data.userId,
                profileName: name,
                phoneNumber: phone
            };
            setTimeout(() => switchScreen('main'), 1000);
            loadMainScreen();
        } else {
            showStatus(data.message, 'error');
        }
    } catch (error) {
        console.error('Register error:', error);
        showStatus('✗ Registration failed', 'error');
    }
}

async function login() {
    const phone = document.getElementById('loginPhone').value.trim();
    
    if (!phone) {
        showStatus('Please enter your phone number', 'error');
        return;
    }
    
    try {
        const response = await fetch(`${API_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ phoneNumber: phone })
        });
        
        const data = await response.json();
        
        if (data.success) {
            showStatus('✓ Login successful!', 'success');
            currentUser = {
                userId: data.userId,
                profileName: data.profileName,
                phoneNumber: phone
            };
            setTimeout(() => switchScreen('main'), 1000);
            loadMainScreen();
        } else {
            showStatus(data.message, 'error');
        }
    } catch (error) {
        console.error('Login error:', error);
        showStatus('✗ Login failed', 'error');
    }
}

function logout() {
    if (confirm('Are you sure you want to logout?')) {
        currentUser = null;
        currentChat = null;
        currentGroup = null;
        switchScreen('login');
        document.getElementById('loginPhone').value = '';
        document.getElementById('registerName').value = '';
        document.getElementById('registerPhone').value = '';
    }
}

function showStatus(message, type) {
    const statusDiv = document.getElementById('loginStatus');
    statusDiv.textContent = message;
    statusDiv.className = `status-message show ${type}`;
    
    if (type === 'success') {
        setTimeout(() => statusDiv.classList.remove('show'), 3000);
    }
}

// ==================== Navigation Functions ====================

function switchScreen(screenName) {
    document.querySelectorAll('.screen').forEach(screen => {
        screen.classList.remove('active');
    });
    document.getElementById(screenName + 'Screen').classList.add('active');
}

function switchSidebar(panel) {
    // Reset friend context when switching tabs
    currentFriendId = null;
    currentFriendName = null;
    
    // Hide all panels
    document.querySelectorAll('.panel').forEach(p => {
        p.classList.remove('active');
    });
    
    // Remove active from buttons
    document.querySelectorAll('.tabs-container .tab-button').forEach(btn => {
        btn.classList.remove('active');
    });
    
    // Show selected panel and activate button
    document.getElementById(panel + '-panel').classList.add('active');
    event.target.classList.add('active');
}

// ==================== Main Screen Loading ====================

async function loadMainScreen() {
    await refreshChats();
    await refreshGroups();
    await refreshFriends();
    populateFriendDropdown();
    
    // Auto-refresh friends every 5 seconds to see newly added friends
    setInterval(async () => {
        await refreshFriends();
        populateFriendDropdown();
    }, 5000);
}

async function refreshChats() {
    try {
        const response = await fetch(`${API_URL}/chats?userId=${currentUser.userId}`);
        const data = await response.json();
        
        const chatsList = document.getElementById('chatsList');
        chatsList.innerHTML = '';
        
        if (data.chats && data.chats.length > 0) {
            for (const chat of data.chats) {
                const chatEl = createChatElement(chat);
                chatsList.appendChild(chatEl);
            }
        } else {
            chatsList.innerHTML = '<div style="padding: 20px; text-align: center; color: #999;">No chats yet</div>';
        }
    } catch (error) {
        console.error('Error loading chats:', error);
    }
}

async function refreshGroups() {
    try {
        const response = await fetch(`${API_URL}/groups?userId=${currentUser.userId}`);
        const data = await response.json();
        
        const groupsList = document.getElementById('groupsList');
        groupsList.innerHTML = '';
        
        if (data.groups && data.groups.length > 0) {
            for (const group of data.groups) {
                const groupEl = createGroupElement(group);
                groupsList.appendChild(groupEl);
            }
        } else {
            groupsList.innerHTML = '<div style="padding: 20px; text-align: center; color: #999;">No groups yet</div>';
        }
    } catch (error) {
        console.error('Error loading groups:', error);
    }
}

async function refreshFriends() {
    try {
        const response = await fetch(`${API_URL}/friends?userId=${currentUser.userId}`);
        const data = await response.json();
        
        const friendsList = document.getElementById('friendsList');
        friendsList.innerHTML = '';
        
        if (data.friends && data.friends.length > 0) {
            for (const friend of data.friends) {
                const friendEl = createFriendElement(friend);
                friendsList.appendChild(friendEl);
            }
        } else {
            friendsList.innerHTML = '<div style="padding: 20px; text-align: center; color: #999;">No friends yet</div>';
        }
    } catch (error) {
        console.error('Error loading friends:', error);
    }
}

async function populateFriendDropdown() {
    if (!currentUser) return;
    
    try {
        const response = await fetch(`${API_URL}/friends?userId=${currentUser.userId}`);
        const data = await response.json();
        
        const select = document.getElementById('friendSelect');
        select.innerHTML = '<option value="">Select a friend...</option>';
        
        if (data.friends && data.friends.length > 0) {
            for (const friend of data.friends) {
                const option = document.createElement('option');
                option.value = friend.friendId;
                option.textContent = friend.profileName + ' (' + friend.phoneNumber + ')';
                select.appendChild(option);
            }
            console.log('✓ Loaded ' + data.friends.length + ' friends for dropdown');
        } else {
            const option = document.createElement('option');
            option.value = '';
            option.textContent = 'No friends yet - add some!';
            option.disabled = true;
            select.appendChild(option);
            console.log('No friends available');
        }
    } catch (error) {
        console.error('Error loading friends for dropdown:', error);
    }
}

// ==================== Chat Functions ====================

function createChatElement(chat) {
    const div = document.createElement('div');
    div.className = 'item';
    div.innerHTML = `
        <div class="item-name">Chat</div>
        <div class="item-subtitle">${chat.messageCount} messages</div>
    `;
    div.onclick = () => openChat(chat.chatId);
    return div;
}

function openChat(chatId) {
    currentChat = chatId;
    currentGroup = null;
    
    document.querySelectorAll('.item').forEach(item => {
        item.classList.remove('active');
    });
    event.target.closest('.item').classList.add('active');
    
    document.getElementById('chatHeader').innerHTML = '<h3>💬 Chat</h3>';
    document.getElementById('chatMessages').innerHTML = '<div style="padding: 20px; color: #999;">Loading messages...</div>';
    document.getElementById('messageInput').placeholder = 'Type a message...';
    
    // Load messages from backend
    loadChatMessages(chatId);
}

async function loadChatMessages(chatId) {
    try {
        const response = await fetch(`${API_URL}/chats/messages?senderId=${currentUser.userId}`);
        const data = await response.json();
        
        const messagesDiv = document.getElementById('chatMessages');
        messagesDiv.innerHTML = '';
        
        if (data.messages && data.messages.length > 0) {
            for (const msg of data.messages) {
                const messageEl = document.createElement('div');
                messageEl.className = 'message sent';
                
                let timeStr = 'just now';
                if (msg.timestamp) {
                    try {
                        const msgDate = typeof msg.timestamp === 'number' ? new Date(msg.timestamp) : new Date();
                        timeStr = msgDate.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
                    } catch (e) {
                        timeStr = 'just now';
                    }
                }
                
                const bubbleDiv = document.createElement('div');
                bubbleDiv.className = 'message-bubble';
                bubbleDiv.textContent = msg.content;
                
                const timeDiv = document.createElement('div');
                timeDiv.className = 'message-time';
                timeDiv.textContent = timeStr;
                
                messageEl.appendChild(bubbleDiv);
                messageEl.appendChild(timeDiv);
                messagesDiv.appendChild(messageEl);
            }
            messagesDiv.scrollTop = messagesDiv.scrollHeight;
            console.log('✓ Loaded ' + data.messages.length + ' messages');
        } else {
            messagesDiv.innerHTML = '<div style="padding: 20px; text-align: center; color: #999;">No messages yet. Start the conversation!</div>';
        }
    } catch (error) {
        console.error('Error loading messages:', error);
        document.getElementById('chatMessages').innerHTML = '<div style="padding: 20px; color: #999;">Error loading messages</div>';
    }
}

async function sendMessage() {
    const input = document.getElementById('messageInput');
    const messageText = input.value.trim();
    
    if (!messageText || !currentUser) return;
    
    try {
        const body = {
            senderId: currentUser.userId,
            content: messageText
        };
        
        // Determine if it's a group or friend chat
        if (currentGroup) {
            body.groupId = currentGroup;
        } else if (currentFriendId) {
            body.chatId = currentChat;
        } else if (currentChat) {
            body.chatId = currentChat;
        }
        
        const response = await fetch(`${API_URL}/chats/send`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
        
        const data = await response.json();
        
        if (data.success) {
            input.value = '';
            // Reload messages based on context
            if (currentGroup) {
                await loadGroupMessages(currentGroup);
            } else if (currentFriendId) {
                await loadFriendMessages(currentFriendId);
            } else if (currentChat) {
                await loadChatMessages(currentChat);
            }
        }
    } catch (error) {
        console.error('Error sending message:', error);
    }
}

// ==================== Group Functions ====================

function createGroupElement(group) {
    const div = document.createElement('div');
    div.className = 'item';
    div.innerHTML = `
        <div class="item-name">${group.groupName}</div>
        <div class="item-subtitle">${group.memberCount} members</div>
    `;
    div.onclick = () => openGroup(group.groupId, group.groupName);
    return div;
}

function openGroup(groupId, groupName) {
    currentGroup = groupId;
    currentChat = null;
    currentFriendId = null;
    currentFriendName = null;
    
    document.querySelectorAll('.item').forEach(item => {
        item.classList.remove('active');
    });
    event.target.closest('.item').classList.add('active');
    
    document.getElementById('chatHeader').innerHTML = `<h3>👥 ${groupName}</h3>`;
    document.getElementById('chatMessages').innerHTML = '<div style="padding: 20px; color: #999;">Loading group messages...</div>';
    document.getElementById('messageInput').placeholder = 'Send a message to group...';
    
    // Load group messages
    loadGroupMessages(groupId);
}

// ==================== Friends Functions ====================

function createFriendElement(friend) {
    const div = document.createElement('div');
    div.className = 'item';
    div.innerHTML = `
        <div class="item-name">${friend.profileName}</div>
        <div class="item-subtitle">${friend.phoneNumber}</div>
    `;
    div.onclick = () => {
        document.querySelectorAll('.item').forEach(item => {
            item.classList.remove('active');
        });
        div.classList.add('active');
        openChatWithFriend(friend.profileName, friend.friendId);
    };
    return div;
}

function openChatWithFriend(friendName, friendId) {
    currentChat = 'friend_' + friendId;
    currentFriendId = friendId;
    currentFriendName = friendName;
    currentGroup = null;
    
    document.getElementById('chatHeader').innerHTML = `<h3>💬 ${friendName}</h3>`;
    document.getElementById('chatMessages').innerHTML = '<div style="padding: 20px; color: #999;">Loading messages...</div>';
    document.getElementById('messageInput').placeholder = `Send message to ${friendName}...`;
    
    loadFriendMessages(friendId);
}

async function loadFriendMessages(friendId) {
    try {
        const response = await fetch(`${API_URL}/chats/messages`);
        const data = await response.json();
        
        const messagesDiv = document.getElementById('chatMessages');
        messagesDiv.innerHTML = '';
        
        let friendMessages = [];
        if (data.messages && data.messages.length > 0) {
            friendMessages = data.messages.filter(msg => 
                msg.senderId === currentUser.userId || msg.senderId === friendId
            );
        }
        
        if (friendMessages.length > 0) {
            for (const msg of friendMessages) {
                const messageEl = document.createElement('div');
                const isSent = msg.senderId === currentUser.userId;
                messageEl.className = 'message ' + (isSent ? 'sent' : 'received');
                
                let timeStr = 'just now';
                if (msg.timestamp) {
                    try {
                        const msgDate = typeof msg.timestamp === 'number' ? new Date(msg.timestamp) : new Date();
                        timeStr = msgDate.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
                    } catch (e) {
                        timeStr = 'just now';
                    }
                }
                
                const bubbleDiv = document.createElement('div');
                bubbleDiv.className = 'message-bubble';
                bubbleDiv.textContent = msg.content;
                
                const timeDiv = document.createElement('div');
                timeDiv.className = 'message-time';
                timeDiv.textContent = timeStr;
                
                messageEl.appendChild(bubbleDiv);
                messageEl.appendChild(timeDiv);
                messagesDiv.appendChild(messageEl);
            }
            messagesDiv.scrollTop = messagesDiv.scrollHeight;
            console.log('✓ Loaded ' + friendMessages.length + ' messages with ' + currentFriendName);
        } else {
            messagesDiv.innerHTML = '<div style="padding: 20px; text-align: center; color: #999;">No messages yet. Start the conversation!</div>';
        }
    } catch (error) {
        console.error('Error loading friend messages:', error);
        document.getElementById('chatMessages').innerHTML = '<div style="padding: 20px; color: #999;">Error loading messages</div>';
    }
}

async function loadGroupMessages(groupId) {
    try {
        const response = await fetch(`${API_URL}/chats/messages?groupId=${groupId}`);
        const data = await response.json();
        
        const messagesDiv = document.getElementById('chatMessages');
        messagesDiv.innerHTML = '';
        
        if (data.messages && data.messages.length > 0) {
            for (const msg of data.messages) {
                const messageEl = document.createElement('div');
                const isSent = msg.senderId === currentUser.userId;
                messageEl.className = 'message ' + (isSent ? 'sent' : 'received');
                
                let timeStr = 'just now';
                if (msg.timestamp) {
                    try {
                        const msgDate = typeof msg.timestamp === 'number' ? new Date(msg.timestamp) : new Date();
                        timeStr = msgDate.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
                    } catch (e) {
                        timeStr = 'just now';
                    }
                }
                
                const bubbleDiv = document.createElement('div');
                bubbleDiv.className = 'message-bubble';
                bubbleDiv.textContent = msg.content;
                
                const timeDiv = document.createElement('div');
                timeDiv.className = 'message-time';
                timeDiv.textContent = timeStr;
                
                messageEl.appendChild(bubbleDiv);
                messageEl.appendChild(timeDiv);
                messagesDiv.appendChild(messageEl);
            }
            messagesDiv.scrollTop = messagesDiv.scrollHeight;
            console.log('✓ Loaded ' + data.messages.length + ' group messages');
        } else {
            messagesDiv.innerHTML = '<div style="padding: 20px; text-align: center; color: #999;">No messages yet. Start the conversation!</div>';
        }
    } catch (error) {
        console.error('Error loading group messages:', error);
        document.getElementById('chatMessages').innerHTML = '<div style="padding: 20px; color: #999;">Error loading messages</div>';
    }
}

// ==================== Dialog Functions ====================

function showDialog(dialogId) {
    document.getElementById('dialog-overlay').classList.add('show');
    document.getElementById(dialogId).classList.add('show');
}

function closeDialog() {
    document.getElementById('dialog-overlay').classList.remove('show');
    document.querySelectorAll('.dialog').forEach(dialog => {
        dialog.classList.remove('show');
    });
}

function showNewChatDialog() {
    showDialog('newChatDialog');
}

function showCreateGroupDialog() {
    // Load friends list for member selection
    loadFriendsForGroupCreation();
    showDialog('createGroupDialog');
}

async function loadFriendsForGroupCreation() {
    if (!currentUser) return;
    
    try {
        const response = await fetch(`${API_URL}/friends?userId=${currentUser.userId}`);
        const data = await response.json();
        
        const membersList = document.getElementById('membersList');
        membersList.innerHTML = '';
        
        if (data.friends && data.friends.length > 0) {
            for (const friend of data.friends) {
                const checkbox = document.createElement('div');
                checkbox.style.padding = '8px';
                checkbox.style.borderBottom = '1px solid #eee';
                checkbox.innerHTML = `
                    <input type="checkbox" class="member-checkbox" value="${friend.friendId}" data-name="${friend.profileName}">
                    <span>${friend.profileName} (${friend.phoneNumber})</span>
                `;
                membersList.appendChild(checkbox);
            }
        } else {
            membersList.innerHTML = '<div style="padding: 10px; color: #999;">No friends to add</div>';
        }
    } catch (error) {
        console.error('Error loading friends for group:', error);
    }
}

function showAddFriendDialog() {
    showDialog('addFriendDialog');
}

async function createNewChat() {
    const friendSelect = document.getElementById('friendSelect');
    const friendId = friendSelect.value;
    
    if (!friendId) {
        alert('Please select a friend');
        return;
    }
    
    // Create or open chat with friend
    closeDialog();
    await refreshChats();
    alert('✓ Chat started!');
}

async function createNewGroup() {
    const groupName = document.getElementById('groupNameInput').value.trim();
    
    if (!groupName) {
        alert('Please enter a group name');
        return;
    }
    
    // Get selected members
    const checkboxes = document.querySelectorAll('.member-checkbox:checked');
    const members = Array.from(checkboxes).map(cb => cb.value);
    
    // Add current user to members
    members.push(currentUser.userId);
    
    try {
        const response = await fetch(`${API_URL}/groups/create`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                groupName: groupName,
                adminId: currentUser.userId,
                members: members
            })
        });
        
        const data = await response.json();
        
        if (data.success) {
            alert('✓ Group created successfully!');
            document.getElementById('groupNameInput').value = '';
            closeDialog();
            await refreshGroups();
        } else {
            alert('✗ Failed to create group');
        }
    } catch (error) {
        console.error('Error creating group:', error);
        alert('✗ Error creating group');
    }
}

async function addFriend() {
    const phoneNumber = document.getElementById('friendPhoneInput').value.trim();
    
    if (!phoneNumber) {
        alert('Please enter a phone number');
        return;
    }
    
    try {
        const response = await fetch(`${API_URL}/friends/add`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                userId: currentUser.userId,
                friendPhoneNumber: phoneNumber
            })
        });
        
        const data = await response.json();
        
        if (data.success) {
            alert('✓ Friend added successfully!');
            document.getElementById('friendPhoneInput').value = '';
            closeDialog();
            await refreshFriends();
            await populateFriendDropdown();
        } else {
            alert(data.message);
        }
    } catch (error) {
        console.error('Error adding friend:', error);
        alert('✗ Error adding friend');
    }
}

async function showProfile() {
    try {
        const response = await fetch(`${API_URL}/profile?userId=${currentUser.userId}`);
        const data = await response.json();
        
        const profileContent = document.getElementById('profileContent');
        profileContent.innerHTML = `
            <p><strong>Name:</strong> ${data.profileName}</p>
            <p><strong>Phone:</strong> ${data.phoneNumber}</p>
            <p><strong>Friends:</strong> ${data.friendCount}</p>
            <p><strong>Chats:</strong> ${data.chatCount}</p>
            <p><strong>Groups:</strong> ${data.groupCount}</p>
        `;
        
        showDialog('profileDialog');
    } catch (error) {
        console.error('Error loading profile:', error);
        alert('✗ Error loading profile');
    }
}

// Close dialog on overlay click
document.addEventListener('click', function(event) {
    if (event.target.id === 'dialog-overlay') {
        closeDialog();
    }
});
