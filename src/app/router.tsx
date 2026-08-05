import { Redirect, Route, Switch } from 'react-router-dom';
import { AppLayout } from './AppLayout';
import { CreateLobbyPage } from '../features/lobby/CreateLobbyPage';
import { HomePage } from '../features/lobby/HomePage';
import { JoinLobbyPage } from '../features/lobby/JoinLobbyPage';
import { LobbyPage } from '../features/lobby/LobbyPage';

export function AppRoutes() {
  return (
    <AppLayout>
      <Switch>
        <Route exact path="/" component={HomePage} />
        <Route path="/lobbies/new" component={CreateLobbyPage} />
        <Route path="/lobbies/join" component={JoinLobbyPage} />
        <Route path="/lobbies/:code" component={LobbyPage} />
        <Redirect to="/" />
      </Switch>
    </AppLayout>
  );
}
